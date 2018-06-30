package Micro.Actions.Combat.Tactics

import Micro.Actions.Action
import Micro.Actions.Combat.Targeting.Filters.{TargetFilter, TargetFilterCombatants, TargetFilterVisibleInRange}
import Micro.Actions.Combat.Targeting.TargetAction
import Micro.Actions.Commands.Attack
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Potshot extends Action {
  
  object PotshotTargetFilter extends TargetFilter {
    override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = (
      TargetFilterVisibleInRange.legal(actor, target)
      && TargetFilterCombatants.legal(actor, target)
    )
  }
  
  object PotshotTarget extends TargetAction(PotshotTargetFilter)
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.canFight       &&
    unit.readyForAttackOrder  &&
    unit.matchups.targetsInRange.nonEmpty
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    PotshotTarget.delegate(unit)
    Attack.delegate(unit)
  }
}
