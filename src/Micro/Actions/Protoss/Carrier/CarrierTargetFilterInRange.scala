package Micro.Actions.Protoss.Carrier

import Micro.Actions.Combat.Targeting.Filters.TargetFilter
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object CarrierTargetFilterInRange extends TargetFilter {
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean =
    (target.pixelDistanceEdge(actor) < 32.0 * 8.0)
}
