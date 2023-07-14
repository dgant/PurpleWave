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
    val centroid  = Maff.centroid(exposed.map(_.pixel))
    val threat    = Maff.minBy(shuttle.matchups.threats)(t => t.pixelDistanceCenter(centroid) - t.pixelRangeAgainst(shuttle))
    val towards   = new ArrayBuffer[Pixel]
    exposed.foreach(ally  => towards += ally.pixel.project(centroid, Shuttling.pickupRadiusCenter(ally)))
    threat.foreach(threat => towards += threat.pixel.project(centroid, threat.pixelDistanceCenter(centroid) + Shuttling.pickupRadiusCenter(exposed.head)))

    if (towards.nonEmpty) {
      val to = MicroPathing.pullTowards(Shuttling.pickupRadiusEdge + Shuttling.pickupRadiusCenter(exposed.head), towards: _*)
      shuttle.agent.toTravel = Some(to)
      if (shuttle.pixelDistanceCenter(to) > 32.0 * 12.0 && shuttle.matchups.framesOfSafety < shuttle.unitClass.framesToTurn180) {
        Retreat.delegate(shuttle)
      }
      Commander.move(shuttle)
    }
  }
}
