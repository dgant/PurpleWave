package Micro.Targeting.Filters

import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterCarrierInRange extends TargetFilter {
  override def appliesTo(actor: FriendlyUnitInfo): Boolean = actor.is(Protoss.Carrier)
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = target.pixelDistanceEdge(actor) < 32.0 * 8.0
}
