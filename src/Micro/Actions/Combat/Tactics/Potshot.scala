package Micro.Actions.Combat.Tactics

import Micro.Actions.Action
import Micro.Actions.Combat.Targeting.Filters.TargetFilterPotshot
import Micro.Actions.Combat.Targeting.Target
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Potshot extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.agent.canFight && unit.readyForAttackOrder
  
  override def perform(unit: FriendlyUnitInfo) {
    Target.choose(unit, TargetFilterPotshot)
    Commander.attack(unit)
  }
}
