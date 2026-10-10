package Micro.Actions.Protoss.Shuttle

import Micro.Agency.Commander
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Tactic.Missions.DropRoute

object ShuttlePickup {
  // Preserve quick local rescues; cross-map boarding commands bypass air routing.
  def distant(shuttle: FriendlyUnitInfo, passenger: UnitInfo): Boolean =
    Protoss.Shuttle(shuttle) && passenger.isOurs && !passenger.flying &&
      shuttle.pixelDistanceCenter(passenger) > 384

  def approach(shuttle: FriendlyUnitInfo, passenger: UnitInfo): Unit = {
    if (ShuttleEscape.start(shuttle)) return
    val route = shuttle.agent.get[DropRoute](this).getOrElse {
      val created = new DropRoute
      shuttle.agent.put(this, created)
      created
    }
    route.next(shuttle.tile, passenger.tile) match {
      case Some(path) =>
        shuttle.agent.decision.set(route.waypoint(shuttle.pixel, path))
        Commander.move(shuttle)
      case None => Commander.stop(shuttle)
    }
  }
}
