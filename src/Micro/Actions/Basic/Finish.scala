package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Finish extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.toFinish.isDefined
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    
    With.commander.rightClick(unit, unit.agent.toFinish.get)
  }
}
