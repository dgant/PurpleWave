package Micro.Actions.Combat.Techniques.Common

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Targeting.Target
import Micro.Actions.Commands.Attack
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object AttackAsSoonAsPossible extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.canAttack
  )
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    Target.delegate(unit)
    val target = unit.agent.toAttack
    if (target.isDefined && unit.framesToGetInRange(target.get) + With.reaction.agencyAverage + unit.unitClass.framesToTurn180 >= unit.cooldownLeft) {
      Attack.delegate(unit)
    } else {
      unit.agent.toAttack = None
    }
  }
}
