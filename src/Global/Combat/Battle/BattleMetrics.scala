package Global.Combat.Battle

import Startup.With
import Types.UnitInfo.UnitInfo
import bwapi.{Position, UnitType}
import Utilities.Enrichment.EnrichPosition._
import Utilities.Enrichment.EnrichUnitType._

object BattleMetrics {
  
  def vanguard(group:BattleGroup, otherGroup:BattleGroup):Position = {
    group.units.minBy(_.distanceSquared(otherGroup.center)).position
  }
  
  def center(group:BattleGroup):Position = {
    val airCenter = group.units.view.map(_.position).centroid
    group.units.view.map(_.position).minBy(_.distanceSquared(airCenter))
  }
  
  def expectedSpread(group:BattleGroup):Int = {
    Math.sqrt(group.units.view.map(_.utype.width + 8).sum).toInt
  }
  
  def actualSpread(group:BattleGroup):Int = {
    Math.max(0,
      (group.units.view.map(unit => Math.max(0, unit.distance(group.center) - unit.range)).sum / group.units.size).toInt
      - group.expectedSpread)
  }
  
  def evaluate(group:BattleGroup, battle:Battle):Int = {
    group.units.view.map(evaluate(_, battle.focus)).sum
  }
  
  def evaluate(unit:UnitInfo):Int = {
  
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
  
    val combatEfficacy = dps * unit.totalHealth * highGroundFactor
    Math.max(0, combatEfficacy).toInt
  }
  
  def evaluate(unit:UnitInfo, position:Position):Int = {
    val distanceDropoff = 16.0
    val distanceCutoff = 32.0 * 4
    val distance = Math.max(0, unit.distance(position) - unit.range)
    val distanceFactor = Math.max(0.0, Math.min(1.0, (distanceCutoff + distanceDropoff - distance )/distanceCutoff))
    
    //Shortcut
    if (distanceFactor == 0) return 0
    
    (distanceFactor * evaluate(unit)).toInt
  }
}
