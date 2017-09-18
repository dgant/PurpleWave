package Micro.Actions.Combat.Attacking

import Micro.Actions.Action
import Micro.Heuristics.Targeting.EvaluateTargets
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object TargetAnything extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.canFight           &&
    unit.agent.toAttack.isEmpty   &&
    unit.canAttack                &&
    unit.matchups.targets.nonEmpty
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val targets = unit.matchups.targets.filterNot(_.is(Zerg.Larva))
    unit.agent.toAttack = EvaluateTargets.best(unit, targets)
  }
}
