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
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val besties = unit
      .metro.map(_.units)
      .getOrElse(unit.zone.units)
      .filter(other =>
        other != unit
        && other.friendly.exists(_.intent.shouldMeld)
        && other.unitClass == unit.unitClass)
    
    if (besties.nonEmpty) {
      val bestBestie = besties.minBy(_.pixelDistanceTravelling(unit.pixel))
      Commander.useTechOnUnit(
        unit,
        if (Protoss.HighTemplar(unit)) Protoss.ArchonMeld else Protoss.DarkArchonMeld,
        bestBestie)
    }
  }
}
