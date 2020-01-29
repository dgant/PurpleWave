package Micro.Actions.Transportation.Caddy

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

object ShuttleAcceptRider extends Action {

  override def allowed(shuttle: FriendlyUnitInfo): Boolean = (
    BeAShuttle.allowed(shuttle)
    && (shuttle.agent.passengers.isEmpty || shuttle.battle.isEmpty)
  )

  protected def pickupNeed(shuttle: FriendlyUnitInfo, hailer: FriendlyUnitInfo): Double = {
    val targetedByScarab = hailer.matchups.enemies.exists(r => r.is(Protoss.Reaver) && r.cooldownLeft > 0) &&
      With.units.inPixelRadius(hailer.pixelCenter, 32*7).exists(s => s.orderTarget.contains(hailer) && s.is(Protoss.Scarab))
    val endangered = hailer.matchups.framesOfSafety < shuttle.framesToTravelTo(hailer.pixelCenter) + 2 * shuttle.unitClass.framesToTurn180
    val sojourning = hailer.agent.toTravel.exists(_.pixelDistance(hailer.pixelCenter) > 32.0 * 20)

    (
      (if (shuttle.teammates.contains(hailer)) 5 else 1)
      + (if (targetedByScarab) 100 else 1)
      + (if (endangered) 10 else 1)
      + (if (sojourning) 1 else 0)
    )
  }

  override protected def perform(shuttle: FriendlyUnitInfo): Unit = {
    val pickupCandidates = With.units.ours
      .view
      .filter(passenger =>
        passenger.unitClass.isReaver
        // It shouldn't waste time NOT picking up Reavers that still need to train Scarabs
        //&& (passenger.scarabCount > 0 || passenger.training || passenger.matchups.framesOfSafety < 0)
      )
      .flatMap(_.friendly)
      .filter(passenger =>
          shuttle.canTransport(passenger)
          && passenger.transport.isEmpty
          && passenger.agent.ride.forall(_ == shuttle)
          && passenger.unitClass.spaceRequired + shuttle.agent.passengers.view.map(_.unitClass.spaceRequired).sum <= shuttle.unitClass.spaceProvided
          && ! shuttle.teammates.exists(otherShuttle =>
            otherShuttle.is(Protoss.Shuttle)
            && otherShuttle.complete
            && otherShuttle.pixelDistanceEdge(passenger) < shuttle.pixelDistanceEdge(passenger)
            && otherShuttle.friendly.exists(_.spaceRemaining >= shuttle.spaceRemaining))
      ).toSeq

    val pickupCandidate = ByOption.maxBy(pickupCandidates)(c => pickupNeed(shuttle, c) / (1.0 + c.pixelDistanceSquared(shuttle)))
    pickupCandidate.foreach(hailer => {
      shuttle.agent.claimPassenger(hailer)
    })
  }
}
