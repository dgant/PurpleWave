package Micro.Actions.Basic

import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Addon extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.toAddon.isDefined
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    Commander.addon(unit, unit.agent.toAddon.get)
  }
}
