package Micro.Actions.Combat.Attacking

import Micro.Actions.Action
import Micro.Actions.Commands.Attack
import Micro.Heuristics.Targeting.EvaluateTargets
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Potshot extends Action {
  
  // If we're off cooldown, take a shot at something already in range.
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.action.canFight &&
    unit.readyForAttackOrder &&
    unit.matchups.targetsInRange.nonEmpty
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    val validTargets = unit.matchups.targetsInRange.filter(_.unitClass.helpsInCombat)
    unit.action.toAttack = EvaluateTargets.best(unit.action, validTargets)
    Attack.delegate(unit)
  }
}
