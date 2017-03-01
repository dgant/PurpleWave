package Global.Combat.Battle

import Startup.With
import Types.UnitInfo.UnitInfo
import bwapi.{Position, UnitType}
import Utilities.Enrichment.EnrichPosition._
import Utilities.Enrichment.EnrichUnitType._

object BattleMetrics {
  
  def update(battle: Battle) {
    List(battle.us, battle.enemy).foreach(group => {
      _removeMissingUnits(group)
      if (isValid(battle)) {
        group.strength = score(group, battle)
        group.center = measureCenter(group)
        group.spread = measureSpread(group)
      }
      else {
        return
      }
    })
    if (isValid(battle)) {
      battle.us.vanguard = battle.us.units.minBy(_.position.distanceSquared(battle.enemy.vanguard)).position
      battle.enemy.vanguard = battle.enemy.units.minBy(_.position.distanceSquared(battle.us.vanguard)).position
    }
  }
  
  def isValid(battle:Battle):Boolean = {
    battle.us.units.nonEmpty && battle.enemy.units.nonEmpty
  }
  
  def score(group:BattleGroup, battle:Battle):Int = {
    group.units.map(_valueUnit(_, battle)).sum
  }
  
  def measureCenter(group:BattleGroup):Position = {
    val airCenter = group.units.map(_.position).centroid
    group.units.map(_.position).minBy(_.distanceSquared(airCenter))
  }
  
  def measureSpread(group:BattleGroup):Int = {
    (group.units.map(unit => Math.max(0, unit.distance(group.center) - unit.range)).sum / group.units.size).toInt
  }
  
  def _removeMissingUnits(group:BattleGroup) {
    group.units.filterNot(_.alive).foreach(group.units.remove)
  }
  
  def _valueUnit(unit:UnitInfo, battle: Battle):Int = {
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
    val distanceFactor = Math.min(6, Math.max(1, 3 + (range - unit.position.getDistance(battle.focus))/32))
    val combatEfficacy = dps * unit.totalHealth
    Math.max(0, highGroundFactor * distanceFactor * combatEfficacy).toInt
  }
}
