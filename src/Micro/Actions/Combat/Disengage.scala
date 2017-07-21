package Micro.Actions.Combat

import Micro.Actions.Action
import Planning.Yolo
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Disengage extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.action.canFlee &&
    unit.canMoveThisFrame &&
    ! Yolo.active
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    
    val trapped = unit.matchups.threats.count(threat =>
      threat.melee
      && threat.topSpeed > unit.topSpeed
      && threat.pixelDistanceFast(unit) < 48.0) > 2
    if (trapped) {
      Potshot.delegate(unit)
    }
    
    // If we're faster than all the threats we can afford to be clever.
    //
    val shouldKite = unit.matchups.threats.forall(threat =>
      threat.topSpeed <= unit.topSpeed &&
      threat.framesToGetInRange(unit, unit.pixelCenter.project(threat.pixelCenter, 64.0)) > 0.0)
    
    if (shouldKite) {
      Kite.delegate(unit)
    }
    else {
      Retreat.consider(unit)
    }
  }
}
