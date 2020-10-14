package Micro.Actions.Combat.Targeting.Filters

import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterCarrierIgnoreInterceptors extends TargetFilter {
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = ! actor.is(Protoss.Carrier) || ! target.is(Protoss.Interceptor)
}