package Micro.Actions.Transportation.Caddy

import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

object ShuttlePickup extends Action {

  override def allowed(shuttle: FriendlyUnitInfo): Boolean = BeAShuttle.allowed(shuttle)

  override protected def perform(shuttle: FriendlyUnitInfo): Unit = {
    val pickupCandidates = shuttle.teammates
      .view
      .filter(passenger =>
        passenger.unitClass.isReaver
        && (passenger.scarabCount > 0 || passenger.training || passenger.matchups.framesOfSafety < 0))
      .flatMap(_.friendly)
      .filter(passenger =>
          (passenger.transport.contains(shuttle) || shuttle.canTransport(passenger))
          && passenger.agent.ride.forall(_ == shuttle)
          && passenger.unitClass.spaceRequired + shuttle.agent.passengers.view.filterNot(_ == passenger).map(_.unitClass.spaceRequired).sum
            <= shuttle.unitClass.spaceProvided
      ).toSeq

    val pickupCandidate = ByOption.maxBy(pickupCandidates)(c => Shuttling.pickupNeed(shuttle, c) / (1.0 + c.pixelDistanceSquared(shuttle)))
    pickupCandidate.foreach(hailer => {
      shuttle.agent.claimPassenger(hailer)
      if (hailer.pixelDistanceTravelling(Shuttling.passengerDestination(hailer)) > Shuttling.dropoffRadius + 96
        || hailer.matchups.framesOfSafety + 24 < hailer.cooldownLeft) {
        shuttle.agent.pickup(hailer)
      }
    })
  }
}
