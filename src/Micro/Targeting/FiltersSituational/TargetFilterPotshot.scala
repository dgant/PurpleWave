package Micro.Targeting.FiltersSituational

import Micro.Targeting.TargetFilter
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterPotshot extends TargetFilter {
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = target.unitClass.attacksOrCastsOrDetectsOrTransports && TargetFilterVisibleInRange.legal(actor, target)
}
