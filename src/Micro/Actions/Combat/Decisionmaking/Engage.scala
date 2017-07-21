package Micro.Actions.Combat.Decisionmaking

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Attacking.Punch
import Micro.Actions.Combat.Maneuvering.Kite
import Micro.Actions.Combat.Tactics.BustWallin
import Micro.Actions.Commands.Attack
import Micro.Heuristics.Targeting.EvaluateTargets
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Engage extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.action.canFight &&
    unit.matchups.targets.nonEmpty
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    Punch.consider(unit)
    BustWallin.consider(unit)
    chooseTarget(unit)
    if ( ! unit.readyForAttackOrder) {
      Kite.delegate(unit)
    }
    Attack.delegate(unit)
  }
  
  def chooseTarget(unit: FriendlyUnitInfo) {
    if (unit.action.toAttack.isDefined) {
      return
    }
    val targets = unit.matchups.targets.filter(target =>
      unit.inRangeToAttackFast(target)
      || target.constructing
      || target.gathering
      || target.repairing
      || With.framesSince(target.lastAttackStartFrame) < 48
      || target.topSpeed < unit.topSpeed * 0.75)
    unit.action.toAttack = EvaluateTargets.best(unit.action, targets)
  }
}
