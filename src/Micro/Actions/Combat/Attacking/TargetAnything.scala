package Micro.Actions.Combat.Attacking

import Micro.Actions.Action
import Micro.Heuristics.Targeting.EvaluateTargets
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object TargetAnything extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.action.canFight          &&
    unit.action.toAttack.isEmpty  &&
    unit.canAttack      &&
    unit.matchups.targets.nonEmpty
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val targets = unit.matchups.targets
    unit.action.toAttack = EvaluateTargets.best(unit.action, targets)
  }
}
