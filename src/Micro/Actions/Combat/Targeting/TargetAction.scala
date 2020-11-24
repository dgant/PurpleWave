package Micro.Actions.Combat.Targeting

import Micro.Actions.Action
import Micro.Actions.Combat.Targeting.Filters._
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class TargetAction(val additionalFiltersRequired: TargetFilter*) extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.agent.canFight
    && unit.agent.toAttack.isEmpty
    && unit.canAttack
    && unit.matchups.targets.nonEmpty
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    unit.agent.toAttack = EvaluateTargets.best(unit, EvaluateTargets.preferredTargets(unit, additionalFiltersRequired: _*))
  }
}

