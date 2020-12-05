package Micro.Actions.Combat.Targeting.Filters

import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterLarvaAndEgg extends TargetFilter {
  
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = (
    ! target.unitClass.isZerg || target.unitClass.rawCanAttack || ! target.isAny(Zerg.Larva, Zerg.Egg)
  )
}
