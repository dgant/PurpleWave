package Micro.Actions.Protoss.Shuttle

import Mathematics.Maff
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object ShuttlePark extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = BeShuttle.allowed(unit) && unit.agent.passengers.exists( ! _.loaded)

  override protected def perform(shuttle: FriendlyUnitInfo): Unit = {
    Maff.minBy(shuttle.agent.passengers
      .filter( ! _.loaded))(_.matchups.framesOfSafety)
      .foreach(passenger => {
        val eta = Math.max(0, (shuttle.pixelDistanceCenter(passenger) - Shuttling.pickupRadius) / (shuttle.topSpeed + passenger.topSpeed))
        val curb = passenger.projectFrames(eta)
        shuttle.agent.toTravel =
          Maff.minBy(shuttle.matchups.threats)(_.framesBeforeAttacking(shuttle))
            .map(threat => curb.radiateRadians(threat.pixel.radiansTo(curb), Shuttling.pickupRadius / 2))
            .orElse(Some(curb))

      if (shuttle.pixelDistanceCenter(shuttle.agent.destination) > 32.0 * 12.0 && shuttle.matchups.framesOfSafety < shuttle.unitClass.framesToTurn180) {
        Retreat.delegate(shuttle)
      }
      Commander.move(shuttle)
    })
  }
}
