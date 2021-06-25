package Micro.Actions.Protoss

import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Meld extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.intent.shouldMeld
    && unit.isAny(Protoss.HighTemplar, Protoss.DarkTemplar)
    && ( ! unit.visibleToOpponents || unit.matchups.threatsInRange.nonEmpty || unit.base.exists(_.owner.isUs))
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val besties = unit.metro.map(_.units).getOrElse(unit.zone.units).filter(u =>
      u != unit
      && u.friendly.exists(_.intent.shouldMeld)
      && u.unitClass == unit.unitClass)
    
    if (besties.nonEmpty) {
      val bestBestie = besties.minBy(_.pixelDistanceEdge(unit))
      Commander.useTechOnUnit(
        unit,
        if (Protoss.HighTemplar(unit)) Protoss.ArchonMeld else Protoss.DarkArchonMeld,
        bestBestie)
    }
  }
}
