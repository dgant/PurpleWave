package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Addon extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.toAddon.isDefined
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    
    With.commander.addon(unit, unit.agent.toAddon.get)
  }
}
