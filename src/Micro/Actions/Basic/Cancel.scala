package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Cancel extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    (
          unit.unitClass.isBuilding
      &&  unit.totalHealth < unit.damageInLastSecond * 2
      &&  ( ! unit.training   || unit.framesBeforeBuildeeComplete < unit.matchups.framesToLiveCurrently)
      &&  ( ! unit.upgrading  || unit.framesBeforeUpgradeComplete < unit.matchups.framesToLiveCurrently)
      &&  ( ! unit.teching    || unit.framesBeforeTechComplete    < unit.matchups.framesToLiveCurrently)
    )
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    With.commander.cancel(unit)
  }
}
