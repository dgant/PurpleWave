package Micro.Actions.Transportation.Caddy

import Lifecycle.With
import Micro.Actions.Action
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
    shuttle.agent.prioritizedPassengers
      .find(_.agent.rideGoal.nonEmpty)
      .foreach(passenger => {
        shuttle.agent.toTravel = passenger.agent.rideGoal
        With.commander.move(shuttle)
      })
  }
}
