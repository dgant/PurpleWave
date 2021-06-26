package Micro.Targeting.FiltersSituational

import Micro.Targeting.FiltersOptional.TargetFilterCombatants
import Micro.Targeting.TargetFilter
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterPotshot extends TargetFilter {
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = TargetFilterVisibleInRange.legal(actor, target) && TargetFilterCombatants.legal(actor, target)
}
