package Micro.Actions.Protoss

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Meld extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.canMeld && (
      unit.is(Protoss.HighTemplar) ||
      unit.is(Protoss.DarkTemplar)
    )
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val besties = unit.zone.units.filter(u =>
      u != unit
      && u.friendly.exists(_.agent.canMeld)
      && u.unitClass == unit.unitClass)
    
    if (besties.nonEmpty) {
      val bestBestie = besties.minBy(_.pixelDistanceCenter(unit))
      if (unit.is(Protoss.HighTemplar)) {
        With.commander.useTechOnUnit(unit, Protoss.ArchonMeld, bestBestie)
      }
      else {
        With.commander.useTechOnUnit(unit, Protoss.DarkArchonMeld, bestBestie)
      }
    }
  }
}
