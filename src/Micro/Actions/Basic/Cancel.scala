package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Cancel extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    ( ! unit.complete || unit.training || unit.upgrading || unit.teching) &&
    unit.unitClass.isBuilding &&
    unit.totalHealth < unit.damageInLastSecond * 2
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    With.commander.cancel(unit)
  }
}
