package Micro.Targeting.Filters

import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterLarvaAndEgg extends TargetFilter {
  simulationSafe = true
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = ! target.unitClass.isZerg || target.unitClass.canAttack || target.isNone(Zerg.Larva, Zerg.Egg)
}
