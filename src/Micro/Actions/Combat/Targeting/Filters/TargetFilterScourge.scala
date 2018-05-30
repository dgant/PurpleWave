package Micro.Actions.Combat.Targeting.Filters

import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterScourge extends TargetFilter {
  
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    if ( ! actor.is(Zerg.Scourge)) return true
    
    if (target.is(Zerg.Overlord)) return false
    if ( ! target.canAttack && ! target.isTransport && ! target.unitClass.isDetector) return false
    
    true
  }
  
}
