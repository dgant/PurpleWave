package Micro.Actions.Basic

import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Addon extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.intent.toBuild.exists(_.isAddon)
  }
  
  override def perform(unit: FriendlyUnitInfo): Unit = {
    Commander.addon(unit, unit.intent.toBuild.get)
  }
}
