package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import Micro.Agency.Commander
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
        val nearbyUnits = With.units.inTileRectangle(unit.tileArea.expand(2, 2))
        val nearbyEggs = nearbyUnits.filter(Zerg.Egg)
        val soonestEgg = ByOption.minBy(nearbyEggs)(_.remainingCompletionFrames)
        if (soonestEgg.exists(_.friendly.exists(_.buildType.isWorker))) {
          shouldRallyToMinerals = true
        }
      } else {
        shouldRallyToMinerals = true
      }
    }
    
    if (shouldRallyToMinerals) {
      unit.base.map(_.heart.pixelCenter).foreach(Commander.rally(unit, _))
      return
    }

    Commander.rally(unit, With.scouting.mostBaselikeEnemyTile.pixelCenter)
  }
}
