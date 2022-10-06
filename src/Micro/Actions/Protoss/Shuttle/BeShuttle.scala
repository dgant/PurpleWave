package Micro.Actions.Protoss.Shuttle

import Micro.Actions.Action
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object BeShuttle extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = Protoss.Shuttle(unit)

  override protected def perform(shuttle: FriendlyUnitInfo): Unit = {
    ShuttleDitchPassengers.apply(shuttle)
    ShuttleAdoptPassenger.apply(shuttle)
    ShuttlePickup.apply(shuttle)
    ShuttleCarry.apply(shuttle)
    ShuttlePark.apply(shuttle)
    ShuttleCircle.apply(shuttle)
  }
}
