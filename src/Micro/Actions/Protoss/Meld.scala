package Micro.Actions.Protoss

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Meld extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.canMeld
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val besties = unit.matchups.allies.filter(u => u.friendly.get.agent.canMeld && u.unitClass == unit.unitClass)
    if (besties.nonEmpty) {
      val bestBestie = besties.minBy(_.pixelDistanceFast(unit))
      if (unit.is(Protoss.HighTemplar)) {
        With.commander.useTechOnUnit(unit, Protoss.ArchonMeld, bestBestie)
      }
      else {
        With.commander.useTechOnUnit(unit, Protoss.DarkArchonMeld, bestBestie)
      }
    }
  }
}
