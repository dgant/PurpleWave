package Micro.Actions.Combat.Specialized

import Lifecycle.With
import Micro.Actions.Action
import Micro.Decisions.MicroValue
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object PsionicStorm extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.is(Protoss.HighTemplar) &&
    unit.energy >= Protoss.PsionicStorm.energyCost &&
    unit.matchups.enemies.nonEmpty
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val dying         = unit.matchups.framesToLiveCurrently < 24.0
    val targets       = unit.matchups.allUnits.filter(e => ! e.unitClass.isBuilding && unit.pixelDistanceFast(e) < 9.0 * 32.0)
    val targetValues  = targets.map(target => (target, valueTarget(target))).toMap
    val targetTiles   = targets.map(target => (target, target.project(12).tileIncluding)).toMap
    val targetsByTile = targetTiles.values.toSet.map(tile => (tile, targetTiles.filter(_._2 == tile).keys)).toMap
    val tileValues    = targetsByTile.keys.map(tile => (tile, tile.adjacent8.flatMap(targetsByTile).toSet.toSeq.map(targetValues).sum)).toMap
    
    if (tileValues.nonEmpty) {
      val bestTile = tileValues.maxBy(_._2)
      if (bestTile._2 > unit.subjectiveValue / 2.0) {
        With.commander.useTechOnPixel(unit, Protoss.PsionicStorm, bestTile._1.pixelCenter)
      }
    }
  }
  
  private def valueTarget(target: UnitInfo): Double = {
    MicroValue.valuePerDamage(target) *
      Math.max(112, target.totalHealth) *
      (if(target.isOurs) -1.0 else if (target.isEnemy) 1.0 else 0.0)
  }
}
