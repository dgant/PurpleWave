package Micro.Actions.Protoss.Carrier

import Micro.Actions.Combat.Targeting.Filters.TargetFilter
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object CarrierTargetFilterIgnoreInterceptors extends TargetFilter {
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean =
    ! target.is(Protoss.Interceptor)
}