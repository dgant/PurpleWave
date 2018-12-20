package Micro.Actions.Transportation.Caddy

import Micro.Actions.Action
import Micro.Actions.Commands.Move
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object ShuttleCarry extends Action {

  override def allowed(shuttle: FriendlyUnitInfo): Boolean = {
    shuttle.is(Protoss.Shuttle) && shuttle.loadedUnits.nonEmpty
  }

  override protected def perform(shuttle: FriendlyUnitInfo): Unit = {
    shuttle.agent.passengers
      .sortBy(_ != Shuttling.mainPassenger(shuttle))
      .flatMap(_.agent.consumePassengerRideGoal)
      .foreach(goal => {
        shuttle.agent.toTravel = Some(goal)
        Move.delegate(shuttle)
      })
  }
}
