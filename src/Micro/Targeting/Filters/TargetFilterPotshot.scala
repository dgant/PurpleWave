package Micro.Targeting.Filters

import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterPotshot extends TargetFilter {
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = TargetFilterVisibleInRange.legal(actor, target) && TargetFilterCombatants.legal(actor, target)
}
