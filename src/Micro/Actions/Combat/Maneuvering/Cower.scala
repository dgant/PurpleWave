package Micro.Actions.Combat.Maneuvering

import Micro.Actions.Action
import Micro.Actions.Combat.Decisionmaking.Disengage
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Cower extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    val isHelpless = ! unit.canAttack
    (unit.agent.canCower || isHelpless) &&
      unit.canMove                      &&
      unit.matchups.threats.nonEmpty    &&
      unit.matchups.threats.exists(threat =>
        threat.topSpeed > unit.topSpeed ||
        threat.framesBeforeAttacking(unit) < 48.0) &&
      ! unit.agent.shouldEngage
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    Disengage.delegate(unit)
  }
}
