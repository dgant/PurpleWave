package Micro.Actions.Transportation.Caddy

import Mathematics.Maff
import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo


object ShuttleCarry extends Action {

  override def allowed(shuttle: FriendlyUnitInfo): Boolean = {
    shuttle.is(Protoss.Shuttle) && shuttle.loadedUnits.nonEmpty
  }

  protected def mainPassenger(shuttle: FriendlyUnitInfo): Option[FriendlyUnitInfo] = {
    Maff.maxBy(shuttle.loadedUnits)(p => p.subjectiveValue + p.frameDiscovered / 10000.0)
  }

  override protected def perform(shuttle: FriendlyUnitInfo): Unit = {
    shuttle.agent.prioritizedPassengers
      .find(_.agent.rideGoal.nonEmpty)
      .foreach(passenger => {
        shuttle.agent.toTravel = passenger.agent.rideGoal
        Commander.move(shuttle)
      })
  }
}
