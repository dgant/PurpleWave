package Micro.Actions.Commands

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Attack extends Action {
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.agent.canFight && unit.agent.toAttack.isDefined
  override def perform(unit: FriendlyUnitInfo): Unit = With.commander.attack(unit)
}
