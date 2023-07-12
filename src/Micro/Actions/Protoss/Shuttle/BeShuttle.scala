package Micro.Actions.Protoss.Shuttle

import Mathematics.Maff
import Mathematics.Points.Pixel
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Agency.Commander
import Micro.Coordination.Pathing.MicroPathing
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable.ArrayBuffer

object BeShuttle extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = Protoss.Shuttle(unit)

  protected def perform(shuttle: FriendlyUnitInfo): Unit = {
    if (ShuttleDitchPassengers(shuttle)) return
    if (ShuttleAdoptPassenger(shuttle)) return

    val allHailers    = shuttle.agent.passengersPrioritized.filter(p => ! p.loaded && p.orderTarget.contains(shuttle))
    val closeHailers  = allHailers.filter(_.pixelDistanceEdge(shuttle) < 256)

    // Emergency pickups
    val emergencyHailers = Maff.orElse(
      closeHailers.filter(_.doomed),
      closeHailers.filter(_.matchups.engagedUpon),
      closeHailers.filter(_.matchups.pixelsToThreatRange.exists(_ < 64)))
    val emergencyHailer = Maff.minBy(emergencyHailers)(_.pixelDistanceEdge(shuttle))
    emergencyHailer.foreach(u => { pickup(shuttle, u); return })

    // Urgent dropoffs
    val allDropoffs   = shuttle.agent.passengersPrioritized.filter(p => p.loaded && p.agent.rideGoal.isDefined)
    val urgentDropoff = Maff.minBy(allDropoffs.filter(p => p.agent.toAttack.isDefined || p.agent.toAttackFrom.isDefined))(p => shuttle.pixelDistanceCenter(p.agent.rideGoal.get))
    urgentDropoff.foreach(u => { heed(shuttle, u); return })

    // Ordinary pickups
    val ordinaryHailer = Maff.minBy(allHailers)(_.pixelDistanceEdge(shuttle))
    ordinaryHailer.foreach(p => { pickup(shuttle, p); return })

    // Ordinary dropoffs
    val ordinaryDropoff = allDropoffs.headOption
    ordinaryDropoff.foreach(p => { heed(shuttle, p); return })

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
  private def heed(shuttle: FriendlyUnitInfo, passenger: FriendlyUnitInfo): Unit = {
    shuttle.agent.toTravel = passenger.agent.rideGoal
    Commander.move(shuttle)
  }

  // Park over our landed passengers
  //
  private def park(shuttle: FriendlyUnitInfo): Unit = {
    val exposed   = shuttle.agent.passengersPrioritized.filterNot(_.airborne).sortBy(_.matchups.pixelsEntangled)
    val centroid  = Maff.centroid(exposed.map(_.pixel))
    val threat    = Maff.minBy(shuttle.matchups.threats)(t => t.pixelDistanceCenter(centroid) - t.pixelRangeAgainst(shuttle))
    val towards   = new ArrayBuffer[Pixel]
    exposed.foreach(ally  => towards += ally.pixel.project(centroid, Shuttling.pickupRadiusCenter(ally)))
    threat.foreach(threat => towards += threat.pixel.project(centroid, threat.pixelDistanceCenter(centroid) + Shuttling.pickupRadiusCenter(exposed.head)))

    val to = MicroPathing.pullTowards(Shuttling.pickupRadiusEdge + Shuttling.pickupRadiusCenter(exposed.head))
    shuttle.agent.toTravel = Some(to)
    if (shuttle.pixelDistanceCenter(to) > 32.0 * 12.0 && shuttle.matchups.framesOfSafety < shuttle.unitClass.framesToTurn180) {
      Retreat.delegate(shuttle)
    }
    Commander.move(shuttle)
  }
}
