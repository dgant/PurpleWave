package Micro.Actions.Combat.Attacking

import Micro.Actions.Action
import Micro.Heuristics.Targeting.EvaluateTargets
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object TargetRelevant extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.canFight           &&
    unit.agent.toAttack.isEmpty   &&
    unit.canAttack                &&
    unit.matchups.targets.nonEmpty
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    
    val targets = unit.matchups.targets.filter(target =>
      target.unitClass.helpsInCombat && (
        unit.inRangeToAttackFast(target)
          || (target.unitClass.isDetector && unit.matchups.alliesIncludingSelf.exists(_.cloaked))
          || target.constructing
          || target.gathering
          || target.repairing
          || target.hasBeenViolentInLastTwoSeconds
          || target.topSpeed < unit.topSpeedChasing
          || target.zone.edges.forall(edge => unit.framesToTravelTo(edge.centerPixel) < target.framesToTravelTo(edge.centerPixel))
        )
    )
    
    unit.agent.toAttack = EvaluateTargets.best(unit, targets)
  }
}
