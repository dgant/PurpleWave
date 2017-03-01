package Global.Information.Combat

import Geometry.Clustering
import Startup.With
import Types.UnitInfo.UnitInfo
import Utilities.Limiter
import bwapi.UnitType
import Utilities.Enrichment.EnrichUnitType._
import Utilities.Enrichment.EnrichPosition._

import scala.collection.mutable

class BattleSimulator {
  
  val battleRange = 32 * 16
  var battles:Iterable[BattleSimulation] = List.empty
  
  val limitBattleDefinition = new Limiter(8, _defineBattles)
  def onFrame() {
    limitBattleDefinition.act()
    _simulateBattles()
  }
  
  def _defineBattles() {
    val ourGroupMaps = Clustering.groupUnits(With.units.ours.filterNot(_.utype.isWorker).filter(_.canFight), battleRange)
    val enemyGroupMaps = Clustering.groupUnits(With.units.enemy.filterNot(_.utype.isWorker).filter(_.canFight), battleRange)
    val ourGroups = ourGroupMaps.map(group => new BattleGroup(group._1.position, group._2))
    val enemyGroups = enemyGroupMaps.map(group => new BattleGroup(group._1.position, group._2))
    battles = _constructBattle(ourGroups, enemyGroups)
  }
  
  def _simulateBattles() {
    battles.foreach(_update)
  }
  
  def _constructBattle(
    ourGroups:  Iterable[BattleGroup],
    theirGroups:Iterable[BattleGroup]):Iterable[BattleSimulation] = {
    
    if (theirGroups.isEmpty) return List.empty
  
    ourGroups.filter(_.units.exists(_.alive)).map(ourGroup => {
      val enemyGroup = theirGroups.filter(_.units.exists(_.alive)).minBy(_.vanguard.getDistance(ourGroup.vanguard))
      val simulation = new BattleSimulation(ourGroup, enemyGroup)
      simulation
    })
  }
  
  def _update(battle: BattleSimulation) {
    _removeMissingUnits(battle.ourGroup.units)
    _removeMissingUnits(battle.enemyGroup.units)
    battle.ourScore = battle.ourGroup.units.map(_valueUnit(_, battle)).sum
    battle.enemyScore = battle.enemyGroup.units.map(_valueUnit(_, battle)).sum
    if (battle.ourGroup.units.nonEmpty && battle.enemyGroup.units.nonEmpty) {
      battle.ourGroup.vanguard = battle.ourGroup.units.minBy(_.position.distanceSquared(battle.enemyGroup.vanguard)).position
      battle.enemyGroup.vanguard = battle.enemyGroup.units.minBy(_.position.distanceSquared(battle.ourGroup.vanguard)).position
    }
  }
  
  def _removeMissingUnits(units:mutable.HashSet[UnitInfo]) {
    units.filterNot(_.alive).foreach(units.remove)
  }
  
  def _valueUnit(unit:UnitInfo, battle: BattleSimulation):Int = {
    //Don't be afraid to fight workers.
    //This is a hack, because it's affecting our expectation of combat, rather than our motivation for fighting
    if (unit.utype.isWorker) return 0
    
    //Fails to account for casters
    //Fails to account for carriers/reavers
    //Fails to account for upgrades (including range upgrades)
    
    var dps = unit.groundDps
    var range = unit.range
    
    if (unit.utype == UnitType.Terran_Bunker) {
      dps = 4 * UnitType.Terran_Marine.groundDps
      range = UnitType.Terran_Marine.range
    }
    
    val highGroundFactorRaw = With.game.getGroundHeight(unit.tilePosition)
    val highGroundMultiplier = 1.3
    val highGroundFactor = highGroundFactorRaw match {
      case 0 => 1.0
      case 1 => 1.0 * highGroundMultiplier
      case 2 => 1.0 * highGroundMultiplier
      case 3 => 1.0 * highGroundMultiplier * highGroundMultiplier
      case 4 => 1.0 * highGroundMultiplier * highGroundMultiplier
      case _ => 1.0 * highGroundMultiplier * highGroundMultiplier * highGroundMultiplier
    }
    val distanceFactor = Math.min(6, Math.max(1, 3 + (range - unit.position.getDistance(battle.focalPoint))/32))
    val combatEfficacy = dps * unit.totalHealth
    Math.max(0, highGroundFactor * distanceFactor * combatEfficacy).toInt
  }
}
