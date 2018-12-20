package Micro.Actions.Transportation.Caddy

import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

object ShuttleAcceptRider extends Action {

  override def allowed(shuttle: FriendlyUnitInfo): Boolean = (
    BeAShuttle.allowed(shuttle)
    && (shuttle.agent.passengers.isEmpty || shuttle.matchups.framesOfSafety > 0)
  )

  override protected def perform(shuttle: FriendlyUnitInfo): Unit = {
    val pickupCandidates = shuttle.teammates
      .view
      .filter(passenger =>
        passenger.unitClass.isReaver
        && (passenger.scarabCount > 0 || passenger.training || passenger.matchups.framesOfSafety < 0))
      .flatMap(_.friendly)
      .filter(passenger =>
          shuttle.canTransport(passenger)
          && passenger.transport.isEmpty
          && passenger.agent.ride.forall(_ == shuttle)
          && shuttle.agent.passengers.view.map(_.unitClass.spaceRequired).sum <= shuttle.unitClass.spaceProvided
      ).toSeq

    val pickupCandidate = ByOption.maxBy(pickupCandidates)(c => Shuttling.pickupNeed(shuttle, c) / (1.0 + c.pixelDistanceSquared(shuttle)))
    pickupCandidate.foreach(hailer => {
      shuttle.agent.claimPassenger(hailer)
    })
  }
}
