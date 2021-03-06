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
      With.units.inPixelRadius(hailer.pixel, 32*7).exists(s => s.orderTarget.contains(hailer) && s.is(Protoss.Scarab))
    val endangered = hailer.matchups.framesOfSafety < shuttle.framesToTravelTo(hailer.pixel) + 2 * shuttle.unitClass.framesToTurn180
    val sojourning = hailer.agent.toTravel.exists(_.pixelDistance(hailer.pixel) > 32.0 * 20)

    (
      (if (shuttle.alliesSquadOrBattle.exists(_ == hailer)) 5 else 1)
      + (if (targetedByScarab) 100 else 1)
      + (if (endangered) 10 else 1)
      + (if (sojourning) 1 else 0)
    )
  }

  override protected def perform(shuttle: FriendlyUnitInfo): Unit = {
    val pickupCandidates = With.units.ours
      .view
      .filter(passenger =>
        passenger.is(Protoss.Reaver) && (
          // If the candidate has different goals than our passenger, and aren't in immediate danger, we can't help them
          shuttle.agent.passengers.isEmpty
          || shuttle.battle.exists(passenger.battle.contains)
          || shuttle.agent.passengers.headOption.map(_.squad).getOrElse(shuttle.squad).forall(passenger.squad.contains))
      )
      .flatMap(_.friendly)
      .filter(passenger =>
          shuttle.canTransport(passenger)
          && passenger.transport.isEmpty
          && passenger.agent.ride.forall(_ == shuttle)
          && passenger.unitClass.spaceRequired + shuttle.agent.passengers.view.map(_.unitClass.spaceRequired).sum <= shuttle.unitClass.spaceProvided
          && ! shuttle.alliesSquadOrBattle.exists(otherShuttle =>
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
