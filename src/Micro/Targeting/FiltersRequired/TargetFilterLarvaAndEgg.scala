package Micro.Targeting.FiltersRequired

import Micro.Targeting.TargetFilter
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterLarvaAndEgg extends TargetFilter {
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = target.isNone(Zerg.Larva, Zerg.Egg)
}
