package Micro.Targeting.FiltersRequired

import Micro.Targeting.TargetFilter
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterType extends TargetFilter {
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = target.isNone(Protoss.Interceptor, Zerg.Larva, Zerg.Egg)
}
