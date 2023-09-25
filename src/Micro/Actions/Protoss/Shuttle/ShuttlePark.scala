package Micro.Actions.Protoss.Shuttle

import Mathematics.Maff
import Mathematics.Points.Pixel
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Agency.Commander
import Micro.Coordination.Pathing.MicroPathing
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable.ArrayBuffer

object ShuttlePark extends Action {

  override def allowed(shuttle: FriendlyUnitInfo): Boolean = BeShuttle.allowed(shuttle) && exposedPassengers(shuttle).nonEmpty

  def exposedPassengers(shuttle: FriendlyUnitInfo): Seq[FriendlyUnitInfo] = {
    shuttle.agent.passengersPrioritized.filterNot(_.airborne)
  }

  override protected def perform(shuttle: FriendlyUnitInfo): Unit = {
    val exposed   = shuttle.agent.passengersPrioritized.filterNot(_.airborne).sortBy(_.matchups.pixelsEntangled)
    val centroid  = Maff.centroid(Maff.orElse(exposed.map(_.pixel), Seq(shuttle.pixel)))
    val threat    = Maff.minBy(shuttle.matchups.threats)(t => t.pixelDistanceCenter(centroid) - t.pixelRangeAgainst(shuttle))
    val goals     = new ArrayBuffer[Pixel]
    exposed.foreach(ally  => goals += ally.pixel)
    threat.foreach(threat => goals += threat.pixel.project(centroid, threat.pixelDistanceCenter(centroid) + Shuttling.pickupRadiusCenter(exposed.head)))

    if (goals.nonEmpty) {
      val to = MicroPathing.pullTowards(12, goals: _*) // TODO: The leash length should ideally be the edge-to-edge pickup radius (32) minus the diameter of a spinning Shuttle (unclear) to ensure that the Shuttle doesn't get out of range
      shuttle.agent.decision.set(to)
      if (shuttle.pixelDistanceCenter(to) > 32.0 * 12.0 && shuttle.matchups.framesOfSafety < shuttle.unitClass.framesToTurn180) {
        Retreat.delegate(shuttle)
      }
      Commander.move(shuttle)
    }
  }
}
