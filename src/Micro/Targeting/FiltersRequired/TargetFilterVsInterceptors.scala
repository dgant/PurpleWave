package Micro.Targeting.FiltersRequired

import Micro.Targeting.TargetFilter
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

/*
  Don't target Interceptors with some unit types
 */
object TargetFilterVsInterceptors extends TargetFilter {
  simulationSafe = true
  override def appliesTo(actor: FriendlyUnitInfo): Boolean = actor.flying // Basically all air units prefer attacking the Carriers directly
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = ! Protoss.Interceptor(target)
}