package Micro.Actions.Combat.Maneuvering

import Micro.Actions.Action
import Micro.Actions.Combat.Decisionmaking.Disengage
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Cower extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    (unit.agent.canCower || ( ! unit.canAttack || unit.is(Protoss.Arbiter))) &&
      unit.canMove                    &&
      unit.matchups.threats.nonEmpty  &&
      unit.matchups.threats.exists(threat =>
        threat.topSpeed > unit.topSpeed ||
        threat.framesBeforeAttacking(unit) < 48.0)
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    Disengage.delegate(unit)
  }
}
