package Micro.Actions.Basic

import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Addon extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.intent.toAddon.isDefined
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    Commander.addon(unit, unit.intent.toAddon.get)
  }
}
