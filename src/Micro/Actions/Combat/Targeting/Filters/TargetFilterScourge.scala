package Micro.Actions.Combat.Targeting.Filters

import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterScourge extends TargetFilter {
  simulationSafe = true
  override def appliesTo(actor: FriendlyUnitInfo): Boolean = actor.is(Zerg.Scourge)
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    if (target.is(Zerg.Overlord)) return false
    if ( ! target.canAttack && ! target.isTransport && ! target.unitClass.isDetector) return false
    true
  }
  
}
