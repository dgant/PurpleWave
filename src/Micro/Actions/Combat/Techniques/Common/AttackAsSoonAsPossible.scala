package Micro.Actions.Combat.Techniques.Common

import Micro.Actions.Action
import Micro.Actions.Combat.Attacking.Target
import Micro.Actions.Commands.Attack
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object AttackAsSoonAsPossible extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.canAttack
  )
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    if (unit.readyForAttackOrder
      || unit.matchups.targetsInRange.isEmpty
      || unit.matchups.targets.forall(t => unit.pixelDistanceEdge(t) > unit.pixelRangeAgainst(t) - 32.0)) {
      Target.delegate(unit)
      Attack.delegate(unit)
    }
  }
}
