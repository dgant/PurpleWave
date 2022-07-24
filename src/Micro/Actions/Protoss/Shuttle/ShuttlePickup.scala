package Micro.Actions.Protoss.Shuttle

import Mathematics.Maff
import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.?

object ShuttlePickup extends Action {

  override def allowed(shuttle: FriendlyUnitInfo): Boolean = {
    BeShuttle.allowed(shuttle) && ! shuttle.agent.passengers.forall(_.loaded)
  }

  private def unloadedPassengers(shuttle: FriendlyUnitInfo): Seq[FriendlyUnitInfo] = {
    shuttle.agent.passengersPrioritized.filterNot(_.loaded)
  }

  private def hailers(shuttle: FriendlyUnitInfo): Seq[FriendlyUnitInfo] = {
    unloadedPassengers(shuttle)
      .filter(_.orderTarget.contains(shuttle))
      .sortBy(_.pixelDistanceEdge(shuttle))
  }

  private def sluggos(shuttle: FriendlyUnitInfo): Seq[FriendlyUnitInfo] = {
    unloadedPassengers(shuttle)
      .filter(u => u.agent.rideGoal.exists(g => u.framesToTravelTo(g) > ?(u.canAttack, u.cooldownMaxGround, 24)))
      .sortBy(_.pixelDistanceEdge(shuttle))
  }

  override protected def perform(shuttle: FriendlyUnitInfo): Unit = {
    Maff.orElse(hailers(shuttle), sluggos(shuttle)).headOption.foreach(hailer => {
      shuttle.agent.toTravel = Some(hailer.pixel)
      Commander.rightClick(shuttle, hailer)
    })
  }
}
