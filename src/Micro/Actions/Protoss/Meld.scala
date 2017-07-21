package Micro.Actions.Protoss

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Meld extends Action {
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.action.canMeld
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val besties = unit.matchups.allies.filter(u => u.action.canMeld)
    if (besties.nonEmpty) {
      val bestBestie = besties.minBy(_.pixelDistanceFast(unit))
      With.commander.useTechOnUnit(unit, Protoss.ArchonMeld, bestBestie)
    }
  }
}
