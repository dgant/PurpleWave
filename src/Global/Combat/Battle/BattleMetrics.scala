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
    group.units.view.map(_.position).minBy(_.pixelDistanceSquared(airCenter))
  }
  
  def evaluate(group:BattleGroup, battle:Battle):Int = {
    group.units.view.map(evaluate).sum
  }
  
  def evaluate(unit:UnitInfo):Int = {
    var dps = unit.groundDps
    
    //Fails to account for casters
    //Fails to account for carriers/reavers
    //Fails to account for upgrades (including range upgrades)
    //Fails to account for medics
  
    if (unit.utype == UnitType.Terran_Bunker) {
      dps = 4 * UnitType.Terran_Marine.groundDps
    }
    
    val highGroundBonus =  With.grids.altitudeBonus.get(unit.tileCenter)
    val visibilityBonus = if (unit.visible) 1 else highGroundBonus
    val combatEfficacy = dps * Math.pow(unit.totalHealth, 1.2) * highGroundBonus * visibilityBonus
    Math.max(0, combatEfficacy).toInt
  }
  
  def evaluate(unit:UnitInfo, position:Position):Int = {
    val distanceDropoff = 16.0
    val distanceCutoff = 32.0 * 4
    val distance = Math.max(0, unit.distance(position) - unit.range)
    val distanceFactor = Math.max(0.0, Math.min(1.0, (distanceCutoff + distanceDropoff - distance ) / distanceCutoff))
    
    //Shortcut
    if (distanceFactor == 0) return 0
    
    (distanceFactor * evaluate(unit)).toInt
  }
}
