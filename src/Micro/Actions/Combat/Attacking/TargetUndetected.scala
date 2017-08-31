package Micro.Actions.Combat.Attacking

import Lifecycle.With
import Micro.Actions.Action
import Micro.Heuristics.Targeting.EvaluateTargets
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object TargetUndetected extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.canFight             &&
    unit.agent.toAttack.isEmpty     &&
    unit.canAttack                  &&
    unit.matchups.targets.nonEmpty  &&
    unit.effectivelyCloaked
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val targets = unit.matchups.targets
      .filterNot  (target => With.grids.enemyDetection.isSet(target.tileIncludingCenter))
      .filter     (target => TargetRelevant.isRelevant(unit, target))
    
    unit.agent.toAttack = EvaluateTargets.best(unit, targets)
  }
}
