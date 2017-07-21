package Micro.Actions.Commands

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Attack extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.action.canFight &&
    unit.action.toAttack.isDefined
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    With.commander.attack(unit, unit.action.toAttack.get)
  }
}
