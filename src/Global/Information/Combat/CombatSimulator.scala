package Global.Information.Combat

import Geometry.Cluster
import Startup.With
import Types.UnitInfo.UnitInfo
import Utilities.Limiter
import bwapi.UnitType
import Utilities.Enrichment.EnrichUnitType._
import Utilities.Enrichment.EnrichPosition._

import scala.collection.mutable

class CombatSimulator {
  
  val combatRange = 32 * 18
  var combats:Iterable[CombatSimulation] = List.empty
  
  val limitCombatIdentification = new Limiter(8, _defineCombats)
  def onFrame() {
    limitCombatIdentification.act()
    _simulateCombats
  }
  
  def _defineCombats() {
    val ourGroupMaps = Cluster.generate(With.units.ours.filter(_.canFight), combatRange)
    val enemyGroupMaps = Cluster.generate(With.units.enemy.filter(_.canFight), combatRange)
    val ourGroups = ourGroupMaps.map(group => new CombatGroup(group._1.position, group._2))
    val enemyGroups = enemyGroupMaps.map(group => new CombatGroup(group._1.position, group._2))
    combats = _buildCombats(ourGroups, enemyGroups)
  }
  
  def _simulateCombats() {
    combats.foreach(_update)
  }
  
  def _buildCombats(
    ourGroups:  Iterable[CombatGroup],
    theirGroups:Iterable[CombatGroup]):Iterable[CombatSimulation] = {
    
    if (theirGroups.isEmpty) return List.empty
  
    ourGroups.filter(_.units.exists(_.alive)).map(ourGroup => {
      val enemyGroup = theirGroups.filter(_.units.exists(_.alive)).minBy(_.vanguard.getDistance(ourGroup.vanguard))
      val simulation = new CombatSimulation(ourGroup, enemyGroup)
      simulation
    })
  }
  
  def _update(simulation: CombatSimulation) {
    _removeMissingUnits(simulation.ourGroup.units)
    _removeMissingUnits(simulation.enemyGroup.units)
    simulation.ourScore = simulation.ourGroup.units.map(_valueUnit(_, simulation)).sum
    simulation.enemyScore = simulation.enemyGroup.units.map(_valueUnit(_, simulation)).sum
    if (simulation.ourGroup.units.nonEmpty && simulation.enemyGroup.units.nonEmpty) {
      simulation.ourGroup.vanguard = simulation.ourGroup.units.minBy(_.position.distanceSquared(simulation.enemyGroup.vanguard)).position
      simulation.enemyGroup.vanguard = simulation.enemyGroup.units.minBy(_.position.distanceSquared(simulation.ourGroup.vanguard)).position
    }
  }
  
  def _removeMissingUnits(units:mutable.HashSet[UnitInfo]) {
    units.filterNot(_.alive).foreach(units.remove)
  }
  
  def _valueUnit(unit:UnitInfo, simulation: CombatSimulation):Int = {
    //Don't be afraid to fight workers.
    //This is a hack, because it's affecting our expectation of combat, rather than our motivation for fighting
    if (unit.unitType.isWorker) return 0
    
    //Fails to account for casters
    //Fails to account for carriers/reavers
    //Fails to account for upgrades (including range upgrades)
    
    var dps = unit.groundDps
    var range = unit.range
    
    if (unit.unitType == UnitType.Terran_Bunker) {
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
    val distanceFactor = Math.min(6, Math.max(1, 3 + (range - unit.position.getDistance(simulation.focalPoint))/32))
    val combatEfficacy = dps * unit.totalHealth
    Math.max(0, highGroundFactor * distanceFactor * combatEfficacy).toInt
  }
}
