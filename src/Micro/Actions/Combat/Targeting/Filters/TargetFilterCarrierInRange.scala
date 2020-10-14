package Micro.Actions.Combat.Targeting.Filters

import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterCarrierInRange extends TargetFilter {
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    if  ( !actor.is(Protoss.Carrier)) return true

    (target.pixelDistanceEdge(actor) < 32.0 * 8.0)
  }
}
