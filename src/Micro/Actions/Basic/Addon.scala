package Micro.Actions.Basic

import Micro.Actions.Action
import Micro.Agency.{BuildIntent, Commander}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Addon extends Action {

  def addonIntent(unit: FriendlyUnitInfo): Option[BuildIntent] = unit.intent.toBuild.find(b => b.unitClass.isAddon && b.startNow)
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = addonIntent(unit).isDefined
  
  override def perform(unit: FriendlyUnitInfo): Unit = {
    addonIntent(unit).foreach(b => Commander.addon(unit, b.unitClass))
  }
}
