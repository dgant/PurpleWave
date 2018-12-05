package Micro.Actions.Transportation.Caddy

import Micro.Actions.Action
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object ShuttleQueue extends Action {

  override def allowed(shuttle: FriendlyUnitInfo): Boolean = {
    shuttle.is(Protoss.Shuttle)
  }

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    // TODO: When are we doing nothing?
    // Wait for units to pick up at the nearest Robotics Facility
  }
}
