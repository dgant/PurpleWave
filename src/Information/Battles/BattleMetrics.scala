package Information.Battles

import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.UnitInfo
import Startup.With
import Utilities.EnrichPosition._
import bwapi.Position

object BattleMetrics {
  
  def vanguard(group:BattleGroup, otherGroup:BattleGroup):Position = {
    group.units.minBy(_.pixelDistanceSquared(otherGroup.center)).pixelCenter
  }
  
  def center(group:BattleGroup):Position = {
    val airCenter = group.units.view.map(_.pixelCenter).centroid
    group.units.view.map(_.pixelCenter).minBy(_.distancePixelsSquared(airCenter))
  }
  
  def contextualDesire(battle:Battle):Double = {
    var desire = 1.0
    val zone = With.geography.zones.filter(_.bwtaRegion.getPolygon.isInside(battle.enemy.vanguard))
    if (zone.exists(_.owner == With.self)) desire *= 2
    return desire
  }
  
  def estimateStrength(group:BattleGroup, battle:Battle):Double = {
    group.units.view.map(estimateStrength).sum
  }
  
  def estimateStrength(unit:UnitInfo):Double = {
    var dps = unit.unitClass.groundDps
    
    //Fails to account for casters
    //Fails to account for upgrades (including range upgrades)
    
    if (unit.unitClass == Terran.Medic) dps = 18.6
    
    //Altitude/doodad misses only apply to ranged units
    val highGroundBonus =  With.grids.altitudeBonus.get(unit.tileCenter)
    val combatEfficacy = dps * Math.pow(unit.totalHealth, 1.2) * highGroundBonus
    Math.max(0, combatEfficacy)
  }
  
  def estimateStrength(unit:UnitInfo, position:Position):Double = {
    val distanceDropoff = 16.0
    val distanceCutoff = 32.0 * 4
    val distance = Math.max(0, unit.pixelDistance(position) - unit.unitClass.maxAirGroundRange)
    val distanceFactor = Math.max(0.0, Math.min(1.0, (distanceCutoff + distanceDropoff - distance ) / distanceCutoff))
    distanceFactor * estimateStrength(unit)
  }
}
