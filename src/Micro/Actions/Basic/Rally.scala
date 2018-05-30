package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

object Rally extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    With.framesSince(unit.lastSetRally) > 24 * (if (unit.unitClass.producesLarva) 15 else 1)
    && unit.unitClass.isBuilding
    && unit.unitClass.trainsGroundUnits
    && unit.canDoAnything
  )
  
  override def perform(unit: FriendlyUnitInfo) {
    var shouldRallyToMinerals = false
    
    if (unit.unitClass.isTownHall) {
      if (unit.unitClass.producesLarva) {
        val nearbyUnits = With.units.inRectangle(unit.tileArea.expand(2, 2))
        val nearbyEggs = nearbyUnits.filter(_.is(Zerg.Egg))
        val soonestEgg = ByOption.minBy(nearbyEggs)(_.remainingCompletionFrames)
        if (soonestEgg.exists(_.friendly.exists(_.buildType.isWorker))) {
          shouldRallyToMinerals = true
        }
      }
      else {
        shouldRallyToMinerals = true
      }
    }
    
    if (shouldRallyToMinerals) {
      val minerals = unit.base.map(_.minerals).getOrElse(Set.empty)
      if (minerals.nonEmpty) {
        val mineral = minerals.minBy(_.pixelDistanceEdge(unit))
        With.commander.rally(unit, mineral.pixelCenter)
        return
      }
    }
    
    With.commander.rally(unit, With.intelligence.mostBaselikeEnemyTile.pixelCenter)
  }
}
