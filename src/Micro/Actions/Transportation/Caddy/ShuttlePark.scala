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
        shuttle.agent.toTravel =
          ByOption.minBy(shuttle.matchups.threats)(_.framesBeforeAttacking(shuttle))
            .map(threat =>
              threat.pixelCenter.project(
                passenger.pixelCenter,
                threat.pixelDistanceCenter(passenger) + Shuttling.pickupRadius))
            .orElse(Some(passenger.projectFrames(
              (shuttle.pixelDistanceEdge(passenger) + Shuttling.pickupRadius)
              / (shuttle.topSpeed + passenger.topSpeed))))

      if (shuttle.pixelDistanceCenter(shuttle.agent.destination) > 32.0 * 12.0 && shuttle.matchups.framesOfSafety < shuttle.unitClass.framesToTurn180) {
        Retreat.delegate(shuttle)
      }

      With.commander.move(shuttle)
    })
  }
}
