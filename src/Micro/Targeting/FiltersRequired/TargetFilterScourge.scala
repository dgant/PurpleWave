package Micro.Targeting.FiltersRequired

import Micro.Targeting.TargetFilter
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterScourge extends TargetFilter {
  simulationSafe = true
  override def appliesTo(actor: FriendlyUnitInfo): Boolean = Zerg.Scourge(actor)
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    if (Zerg.Overlord(target)) return false
    if (Protoss.Interceptor(target)) return false
    target.unitClass.attacksOrCastsOrDetectsOrTransports
  }
  
}
