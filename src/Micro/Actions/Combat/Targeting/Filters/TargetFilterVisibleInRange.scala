package Micro.Actions.Combat.Targeting.Filters

import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterVisibleInRange extends TargetFilter {
  simulationSafe = true
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = target.visible && actor.inRangeToAttack(target)
}
