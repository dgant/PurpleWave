package Micro.Actions.Transportation

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object DropOff extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.isTransport                                  &&
    unit.loadedUnits.nonEmpty                         &&
    With.grids.walkable.get(unit.tileIncludingCenter) &&
    unit.agent.toTravel.exists(destination =>
      unit.pixelDistanceFast(destination) < 32.0 * 12.0 &&
      unit.pixelCenter.zone == destination.zone)
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    With.commander.unload(unit, unit.loadedUnits.head)
  }
}
