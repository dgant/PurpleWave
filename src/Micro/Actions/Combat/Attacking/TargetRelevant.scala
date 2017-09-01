package Micro.Actions.Combat.Attacking

import Micro.Actions.Action
import Micro.Heuristics.Targeting.EvaluateTargets
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetRelevant extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.canFight           &&
    unit.agent.toAttack.isEmpty   &&
    unit.canAttack                &&
    unit.matchups.targets.nonEmpty
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val targets = unit.matchups.targets.filter(target => isRelevant(unit, target))
    unit.agent.toAttack = EvaluateTargets.best(unit, targets)
  }
  
  def isRelevant(unit: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    target.unitClass.helpsInCombat && (
      unit.inRangeToAttackFast(target)
        || (target.unitClass.isDetector && unit.matchups.alliesIncludingSelf.exists(_.cloaked))
        || target.constructing
        || target.gathering
        || target.repairing
        || target.hasBeenViolentInLastTwoSeconds
        || target.topSpeed < unit.topSpeedChasing
        || target.zone.edges.forall(edge => unit.framesToTravelTo(edge.centerPixel) < target.framesToTravelTo(edge.centerPixel))
        || (target.is(Zerg.LurkerEgg) && target.matchups.enemyDetectors.isEmpty)
      )
  }
}
