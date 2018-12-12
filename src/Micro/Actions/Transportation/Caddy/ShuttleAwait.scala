package Micro.Actions.Transportation.Caddy

import Micro.Actions.Action
import Micro.Actions.Commands.Move
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

object ShuttleAwait extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.is(Protoss.Shuttle) && unit.agent.passengers.exists( ! _.loaded)

  override protected def perform(shuttle: FriendlyUnitInfo): Unit = {
    ByOption.minBy(shuttle.agent.passengers.filter( ! _.loaded))(_.matchups.framesOfSafety).foreach(passenger => {
      shuttle.agent.toTravel =
        ByOption.minBy(shuttle.matchups.threats)(_.framesBeforeAttacking(shuttle))
          .map(threat => threat.pixelCenter.project(passenger.pixelCenter, threat.pixelDistanceCenter(passenger) + Shuttling.pickupRadius))
          .orElse(Some(passenger.pixelCenter))
      Move.delegate(shuttle)
    })
  }
}
