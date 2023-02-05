package Micro.Targeting.FiltersRequired

import Micro.Targeting.TargetFilter
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterScourge extends TargetFilter {
  override def appliesTo(actor: FriendlyUnitInfo): Boolean = Zerg.Scourge(actor)
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    if (target.unitClass.isBuilding) return false
    target.isNone(Protoss.Interceptor, Zerg.Overlord)
  }
}
