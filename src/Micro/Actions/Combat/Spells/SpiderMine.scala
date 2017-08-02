package Micro.Actions.Combat.Spells

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object SpiderMine extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.is(Terran.Vulture)                             &&
    With.self.hasTech(Terran.SpiderMinePlant)           &&
    unit.spiderMines > 0                                &&
    ! unit.pixelCenter.zone.owner.isUs                  &&
    unit.matchups.targets.exists( ! _.unitClass.floats) &&
    ! unit.matchups.allies.exists(ally => ! ally.unitClass.floats && ally.pixelDistanceFast(unit) < 96.0)
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    With.commander.useTechOnPixel(unit, Terran.SpiderMinePlant, unit.pixelCenter)
  }
}
