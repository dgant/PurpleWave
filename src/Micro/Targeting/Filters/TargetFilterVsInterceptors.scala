package Micro.Targeting.Filters

import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterVsInterceptors extends TargetFilter {
  simulationSafe = true
  override def appliesTo(actor: FriendlyUnitInfo): Boolean = actor.is(Protoss.Carrier)
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = ! actor.is(Protoss.Carrier) || ! target.is(Protoss.Interceptor)
}