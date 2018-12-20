package Micro.Actions.Transportation.Caddy

import Micro.Actions.Action
import Micro.Actions.Commands.Move
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

object ShuttleCarry extends Action {

  override def allowed(shuttle: FriendlyUnitInfo): Boolean = {
    shuttle.is(Protoss.Shuttle) && shuttle.loadedUnits.nonEmpty
  }

  protected def mainPassenger(shuttle: FriendlyUnitInfo): Option[FriendlyUnitInfo] = {
    ByOption.maxBy(shuttle.loadedUnits)(p => p.subjectiveValue + p.frameDiscovered / 10000.0)
  }

  override protected def perform(shuttle: FriendlyUnitInfo): Unit = {
    shuttle.agent.passengers
      .sortBy(_ != mainPassenger(shuttle))
      .flatMap(_.agent.consumePassengerRideGoal)
      .foreach(goal => {
        shuttle.agent.toTravel = Some(goal)
        Move.delegate(shuttle)
      })
  }
}
