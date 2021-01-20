package Micro.Actions.Protoss

import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Meld extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.agent.canMeld
    && (unit.is(Protoss.HighTemplar) || unit.is(Protoss.DarkTemplar))
    && ( ! unit.visibleToOpponents || unit.matchups.threatsInRange.nonEmpty || unit.base.exists(_.owner.isUs))
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val besties = unit.zone.units.filter(u =>
      u != unit
      && u.friendly.exists(_.agent.canMeld)
      && u.unitClass == unit.unitClass)
    
    if (besties.nonEmpty) {
      val bestBestie = besties.minBy(_.pixelDistanceEdge(unit))
      if (unit.is(Protoss.HighTemplar)) {
        Commander.useTechOnUnit(unit, Protoss.ArchonMeld, bestBestie)
      }
      else {
        Commander.useTechOnUnit(unit, Protoss.DarkArchonMeld, bestBestie)
      }
    }
  }
}
