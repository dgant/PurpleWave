package Micro.Actions.Combat.Attacking

import Micro.Actions.Combat.Attacking.Filters.TargetFilter
import Micro.Heuristics.Targeting.EvaluateTargets
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object ApplyTargetFilters {
  
  def apply(actor: FriendlyUnitInfo, filters: Seq[TargetFilter]) {
    if (actor.agent.toAttack.isDefined) return
    def audit = { actor.matchups.targets.map(target => (target, filters.map(f => (f, f.legal(actor, target))))) }
    val targets = actor.matchups.targets.filter(target => filters.forall(_.legal(actor, target)))
    actor.agent.toAttack = EvaluateTargets.best(actor, targets)
  }
}
