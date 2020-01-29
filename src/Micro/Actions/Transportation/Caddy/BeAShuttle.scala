package Micro.Actions.Transportation.Caddy

import Micro.Actions.Action
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object BeAShuttle extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.is(Protoss.Shuttle)

  override protected def perform(shuttle: FriendlyUnitInfo): Unit = {
    ShuttleAcceptRider.consider(shuttle)
    ShuttleRegroup.consider(shuttle)
    ShuttleCarry.consider(shuttle)
    ShuttlePark.consider(shuttle)
    ShuttleCircle.consider(shuttle)
  }
}
