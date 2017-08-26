package Micro.Actions.Combat.Attacking

import Micro.Actions.Action
import Micro.Actions.Commands.Attack
import Micro.Heuristics.Targeting.EvaluateTargets
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Potshot extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.canFight       &&
    unit.readyForAttackOrder  &&
    unit.matchups.targetsInRange.nonEmpty
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    val validTargets = unit.matchups.targetsInRange.filter(_.unitClass.helpsInCombat)
    unit.agent.toAttack = EvaluateTargets.best(unit, validTargets)
    
    Attack.delegate(unit)
  }
}
