package Micro.Targeting.FiltersRequired

import Micro.Targeting.TargetFilter
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterType extends TargetFilter {
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = (
    (Protoss.Reaver(actor) || target.isNone(Zerg.Larva, Zerg.Egg)) &&
    ( ! Protoss.Interceptor(target) || actor.isAny(Terran.Marine, Protoss.Archon, Zerg.Hydralisk)))
}
