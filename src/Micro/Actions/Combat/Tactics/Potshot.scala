package Micro.Actions.Combat.Tactics

import Micro.Actions.Action
import Micro.Targeting.Target
import Micro.Agency.Commander
import Micro.Targeting.FiltersSituational.TargetFilterPotshot
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Potshot extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.intent.canFight && unit.readyForAttackOrder
  
  override def perform(unit: FriendlyUnitInfo): Unit = {
    Target.choose(unit, TargetFilterPotshot)
    Commander.attack(unit)
  }
}
