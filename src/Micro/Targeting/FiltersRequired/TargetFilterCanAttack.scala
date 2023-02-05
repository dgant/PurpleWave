package Micro.Targeting.FiltersRequired

import Micro.Targeting.TargetFilter
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterCanAttack extends TargetFilter {
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = actor.canAttack(target)
}
