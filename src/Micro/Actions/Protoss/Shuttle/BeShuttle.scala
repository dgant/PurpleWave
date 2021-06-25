package Micro.Actions.Protoss.Shuttle

import Micro.Actions.Action
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object BeShuttle extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = Protoss.Shuttle(unit)

  override protected def perform(shuttle: FriendlyUnitInfo): Unit = {
    ShuttleAcceptRider.consider(shuttle)
    ShuttleRegroup.consider(shuttle)
    ShuttleCarry.consider(shuttle)
    ShuttlePark.consider(shuttle)
    ShuttleCircle.consider(shuttle)
  }
}
