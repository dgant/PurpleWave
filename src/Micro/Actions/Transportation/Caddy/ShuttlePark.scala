package Micro.Actions.Transportation.Caddy

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

object ShuttlePark extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.is(Protoss.Shuttle) && unit.agent.passengers.exists( ! _.loaded)

  override protected def perform(shuttle: FriendlyUnitInfo): Unit = {
    ByOption.minBy(shuttle.agent.passengers
      .filter( ! _.loaded))(_.matchups.framesOfSafety)
      .foreach(passenger => {
        val eta = Math.max(0, (shuttle.pixelDistanceCenter(passenger) - Shuttling.pickupRadius) / (shuttle.topSpeed + passenger.topSpeed))
        val curb = passenger.projectFrames(eta)
        shuttle.agent.toTravel =
          ByOption.minBy(shuttle.matchups.threats)(_.framesBeforeAttacking(shuttle))
            .map(threat => curb.radiateRadians(threat.pixel.radiansTo(curb), Shuttling.pickupRadius / 2))
            .orElse(Some(curb))

      if (shuttle.pixelDistanceCenter(shuttle.agent.destination) > 32.0 * 12.0 && shuttle.matchups.framesOfSafety < shuttle.unitClass.framesToTurn180) {
        Retreat.delegate(shuttle)
      }
      With.commander.move(shuttle)
    })
  }
}
