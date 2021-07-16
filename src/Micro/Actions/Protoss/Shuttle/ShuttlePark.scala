package Micro.Actions.Protoss.Shuttle

import Mathematics.Maff
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object ShuttlePark extends Action {

  override def allowed(shuttle: FriendlyUnitInfo): Boolean = BeShuttle.allowed(shuttle) && exposedPassengers(shuttle).nonEmpty

  def exposedPassengers(shuttle: FriendlyUnitInfo) = shuttle.agent.passengers.view.filterNot(_.transport.contains(shuttle))

  override protected def perform(shuttle: FriendlyUnitInfo): Unit = {
    val radius = Shuttling.pickupRadius
    val exposed = exposedPassengers(shuttle)
    var to = exposed.head.pixel.project(shuttle.agent.safety, radius / 2)
    if (exposed.size > 1) {
      val centroid = Maff.centroid(exposed.view.map(_.pixel))
      val slack = exposed.map(_.pixelDistanceCenter(centroid)).max
      if (slack < radius) {
        to = centroid.project(shuttle.agent.safety, slack)
      }
    }
    shuttle.agent.toTravel = Some(to)
    if (shuttle.pixelDistanceCenter(to) > 32.0 * 12.0 && shuttle.matchups.framesOfSafety < shuttle.unitClass.framesToTurn180) {
      Retreat.delegate(shuttle)
    }
    Commander.move(shuttle)
  }
}
