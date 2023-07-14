package Micro.Actions.Protoss.Shuttle

import Mathematics.Maff
import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.?

object BeShuttle extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = Protoss.Shuttle(unit)

  protected def perform(shuttle: FriendlyUnitInfo): Unit = {
    if (ShuttleDitchPassengers(shuttle)) return
    if (ShuttleAdoptPassenger(shuttle)) return

    // Emergency pickups
    val hailers           = shuttle.agent.passengersPrioritized.filter(p => ! p.loaded && p.agent.wantsPickup)
    val hailersMostUrgent = Maff.orElse(
      hailers.filter(_.doomed),
      hailers.filter(_.likelyDoomed),
      hailers.filter(_.likelyDoomedInFrames < 24),
      hailers.filter(_.matchups.engagedUpon),
      hailers.filter(_.matchups.pixelsToThreatRange.exists(_ < 64)),
      hailers)
    val emergencyHailer = Maff.minBy(hailersMostUrgent)(_.pixelDistanceEdge(shuttle))
    emergencyHailer.foreach(u => {
      pickup(shuttle, u)
      shuttle.agent.act("EmergencyPickup")
      return
    })

    // Urgent dropoffs
    /*
    val allDropoffs   = shuttle.agent.passengersPrioritized.filter(p => p.loaded && p.agent.rideGoal.isDefined)
    val urgentDropoff = Maff.minBy(allDropoffs.filter(p => p.agent.toAttack.isDefined || p.agent.toAttackFrom.isDefined))(p => shuttle.pixelDistanceCenter(p.agent.rideGoal.get))
    urgentDropoff.foreach(u => {
      deliver(shuttle, u)
      shuttle.agent.act("UrgentDropoff")
      return
    })
    */

    // Ordinary pickups/dropoffs/parking
    val quests = shuttle.agent.passengersPrioritized.flatMap(p => {
      val goal = p.agent.rideGoal
      if (p.loaded) {

        goal.map(g => (
          p,
          "Deliver",
          shuttle.pixelDistanceCenter(g),
          () => deliver(shuttle, p)))
      } else if (
          goal.exists(g => p.framesToTravelTo(g) > 24 + ?(p.canAttack, p.unitClass.cooldownOnDrop, 0))
          && p.agent.toAttack.forall(t => Math.min(p.pixelsToGetInRange(t), p.pixelsToGetInRange(t, t.projectFrames(p.unitClass.cooldownOnDrop))) > 32)) {

        goal.map(g => (
          p,
          "Pickup",
          shuttle.pixelDistanceEdge(p) - p.pixelDistanceCenter(g) * Maff.nanToOne(shuttle.topSpeed / p.topSpeed),
          () => pickup(shuttle, p)))
      } else if (p.agent.toAttack.isDefined || p.agent.toAttackFrom.isDefined || p.team.exists(_.engagedUpon) || p.matchups.pixelsToThreatRange.exists(_ < 320)) {

        goal.map(g => (
          p,
          "Park",
          shuttle.pixelDistanceEdge(p) + 160,
          () => ShuttlePark(shuttle)))
      } else None
    })

    val quest = Maff.minBy(quests)(_._3)
    quest.foreach(q => {
      q._4()
      shuttle.agent.act(q._2)
      return
    })

    ShuttlePark(shuttle)
    ShuttleAwait(shuttle)
  }

  // Pick up a passenger
  //
  private def pickup(shuttle: FriendlyUnitInfo, passenger: FriendlyUnitInfo): Unit = {
    shuttle.agent.toTravel = Some(passenger.pixel)
    Commander.rightClick(shuttle, passenger)
  }

  // Heed a passenger's ride goal
  //
  private def deliver(shuttle: FriendlyUnitInfo, passenger: FriendlyUnitInfo): Unit = {
    shuttle.agent.toTravel = passenger.agent.rideGoal
    Commander.move(shuttle)
  }
}
