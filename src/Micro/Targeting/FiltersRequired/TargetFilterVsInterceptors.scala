package Micro.Targeting.FiltersRequired

import Micro.Targeting.TargetFilter
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterVsInterceptors extends TargetFilter {
  override def appliesTo(actor: FriendlyUnitInfo): Boolean = actor.flying // Basically all air units prefer attacking the Carriers directly. Corsairs don't even do splash damage
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = ! Protoss.Interceptor(target)
}