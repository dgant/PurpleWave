package Micro.Actions.Transportation

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object DropOff extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.isTransport                                  &&
    unit.loadedUnits.nonEmpty                         &&
    With.grids.walkable.get(unit.tileIncludingCenter)
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val fastestPassenger  = unit.loadedUnits.maxBy(_.topSpeed)
    val inZone            = unit.agent.toTravel.forall(_.zone == unit.zone)
    val onRunway          = unit.agent.toTravel.forall(unit.pixelDistanceFast(_) < 32.0 * 6.0)
    val inHurry           = unit.matchups.framesToLiveCurrently < 24 + 24 * unit.loadedUnits.size + unit.agent.toTravel.map(unit.framesToTravelTo).sum
    
    if (inZone && (onRunway || inHurry)) {
      With.commander.unload(unit, unit.loadedUnits.head)
    }
  }
}
