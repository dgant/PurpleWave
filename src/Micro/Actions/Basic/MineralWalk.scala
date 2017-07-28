package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object MineralWalk extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.toGather.isDefined
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    With.commander.gather(unit, unit.agent.toGather.get)
  }
}
