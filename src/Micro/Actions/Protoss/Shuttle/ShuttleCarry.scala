package Micro.Actions.Protoss.Shuttle

import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object ShuttleCarry extends Action {

  override def allowed(shuttle: FriendlyUnitInfo): Boolean = BeShuttle.allowed(shuttle) && shuttle.loadedUnits.nonEmpty

  override protected def perform(shuttle: FriendlyUnitInfo): Unit = {
    shuttle.agent.prioritizedPassengers
      .find(_.agent.rideGoal.nonEmpty)
      .foreach(passenger => {
        shuttle.agent.toTravel = passenger.agent.rideGoal
        Commander.move(shuttle)
      })
  }
}
