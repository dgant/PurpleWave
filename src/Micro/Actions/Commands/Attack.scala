package Micro.Actions.Commands

import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Attack extends Action {
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.intent.canFight && unit.agent.toAttack.isDefined
  override def perform(unit: FriendlyUnitInfo): Unit = Commander.attack(unit)
}
