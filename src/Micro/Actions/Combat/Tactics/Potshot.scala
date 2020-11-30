package Micro.Actions.Combat.Tactics

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Targeting.Target
import Micro.Actions.Combat.Targeting.Filters.TargetFilterPotshot
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Potshot extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.agent.canFight && unit.readyForAttackOrder
  
  override def perform(unit: FriendlyUnitInfo) {
    Target.choose(unit, TargetFilterPotshot)
    With.commander.attack(unit)
  }
}
