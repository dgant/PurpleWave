package Micro.Actions.Transportation.Caddy

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Commands.Move
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

object ShuttleCircle extends Action {
  override def allowed(unit: FriendlyUnitInfo): Boolean = BeAShuttle.allowed(unit) && unit.agent.passengers.isEmpty

  override protected def perform(shuttle: FriendlyUnitInfo): Unit = {
    shuttle.agent.toTravel =
      ByOption.minBy(With.units.ours.view.filter(_.is(Protoss.RoboticsFacility)))(_.pixelDistanceCenter(shuttle))
        .map(_.pixelCenter)
        .orElse(shuttle.agent.toTravel)
    Move.delegate(shuttle)
  }
}
