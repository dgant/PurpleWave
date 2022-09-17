package Micro.Actions.Protoss.Shuttle

import Lifecycle.With
import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object ShuttleDitchPassengers extends Action {
  override def allowed(shuttle: FriendlyUnitInfo): Boolean = Protoss.Shuttle(shuttle)
  override protected def perform(shuttle: FriendlyUnitInfo): Unit = {
    if (shuttle.base.exists(_.owner.isEnemy) && ! shuttle.agent.destination.base.exists(_.owner.isEnemy)) return
    val hitchhikers = (shuttle.agent.passengers ++ shuttle.loadedUnits)
      .distinct
      .filter(p => ! p.isAny(Protoss.Reaver, Protoss.HighTemplar) && ! p.squad.exists(shuttle.squad.contains))
      .toVector
    With.logger.debug(f"$shuttle ${shuttle.squad.map(_.toString).getOrElse("(No squad")} ditching ${hitchhikers.map(h => f"$h ${h.squad.map(_.toString).getOrElse("(No squad)")}").mkString(", ")}")
    hitchhikers.foreach(shuttle.agent.removePassenger)
    hitchhikers.foreach(Commander.unload(shuttle, _))
  }
}
