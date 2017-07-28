package Micro.Actions.Combat.Attacking

import Lifecycle.With
import Micro.Actions.Action
import Micro.Heuristics.Targeting.EvaluateTargets
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object TargetRelevant extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.action.canFight          &&
    unit.action.toAttack.isEmpty  &&
    unit.canAttack      &&
    unit.matchups.targets.nonEmpty
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val targets = unit.matchups.targets.filter(target =>
      unit.inRangeToAttackFast(target)
        || target.constructing
        || target.gathering
        || target.repairing
        || With.framesSince(target.lastAttackStartFrame) < 48
        || target.topSpeed < unit.topSpeed * 0.75)
    unit.action.toAttack = EvaluateTargets.best(unit, targets)
  }
}
