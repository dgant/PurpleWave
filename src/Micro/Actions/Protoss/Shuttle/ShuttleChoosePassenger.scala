package Micro.Actions.Protoss.Shuttle

import Lifecycle.With
import Mathematics.Maff
import Micro.Actions.Action
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object ShuttleChoosePassenger extends Action {

  override def allowed(shuttle: FriendlyUnitInfo): Boolean = (
    BeShuttle.allowed(shuttle)
    && (shuttle.agent.passengers.isEmpty || shuttle.battle.isEmpty)
  )

  protected def pickupNeed(shuttle: FriendlyUnitInfo, hailer: FriendlyUnitInfo): Double = {
    lazy val targetedByScarab = With.unitsShown.allEnemies(Protoss.Reaver) > 0 && With.units.inPixelRadius(hailer.pixel, 32*7).exists(s => Protoss.Scarab(s) && s.orderTarget.exists(_.pixelDistanceEdge(hailer) < 48))
    lazy val endangered = hailer.matchups.framesOfSafety < shuttle.framesToTravelTo(hailer.pixel) + 2 * shuttle.unitClass.framesToTurn180
    lazy val sojourning = hailer.agent.toTravel.exists(_.pixelDistance(hailer.pixel) > 32.0 * 20)
    (
      (if (Protoss.Reaver(hailer)) 1000 else 1)
      * (
        (if (shuttle.alliesSquadOrBattle.exists(hailer==)) 5 else 1)
        + (if (targetedByScarab) 100 else 1)
        + (if (endangered) 10 else 1)
        + (if (sojourning) 1 else 0)))
  }

  override protected def perform(shuttle: FriendlyUnitInfo): Unit = {
    val pickupCandidates = With.units.ours
      .view
      .filter(passenger =>
        passenger.isAny(Protoss.Reaver, Protoss.HighTemplar) && (
          shuttle.agent.passengers.isEmpty
          || shuttle.agent.passengers.headOption.map(_.squad).getOrElse(shuttle.squad).forall(passenger.squad.contains)))
      .flatMap(_.friendly)
      .filter(passenger =>
        shuttle.canTransport(passenger)
        && passenger.transport.isEmpty
        && passenger.agent.ride.forall(_ == shuttle)
        && passenger.unitClass.spaceRequired + shuttle.agent.passengers.view.map(_.unitClass.spaceRequired).sum <= shuttle.unitClass.spaceProvided
        && ! shuttle.alliesSquadOrBattle.exists(otherShuttle =>
          Protoss.Shuttle(otherShuttle)
          && otherShuttle.complete
          && otherShuttle.pixelDistanceEdge(passenger) < shuttle.pixelDistanceEdge(passenger)
          && otherShuttle.friendly.exists(_.spaceRemaining >= shuttle.spaceRemaining)))
      .toVector

    val candidate = Maff.maxBy(pickupCandidates)(c => pickupNeed(shuttle, c) / (1.0 + c.pixelDistanceSquared(shuttle)))
    candidate.foreach(shuttle.agent.addPassenger)
  }
}
