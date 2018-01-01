package Micro.Actions.Combat.Attacking.Filters

import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterInRange extends TargetFilter {
  
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    actor.inRangeToAttackFast(target)
  }
  
}
