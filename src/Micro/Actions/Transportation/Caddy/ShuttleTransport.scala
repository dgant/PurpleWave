package Micro.Actions.Transportation.Caddy

import Micro.Actions.Action
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object ShuttleTransport extends Action {

  override def allowed(shuttle: FriendlyUnitInfo): Boolean = shuttle.is(Protoss.Shuttle)

  override protected def perform(shuttle: FriendlyUnitInfo): Unit = {

  }
}
