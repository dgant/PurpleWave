package Micro.Actions.Combat.Decisionmaking

import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.{Kite, Retreat}
import Planning.Yolo
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Disengage extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.action.canFlee   &&
    unit.canMoveThisFrame &&
    ! Yolo.active
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    
    def kiteable(enemy: UnitInfo): Boolean = {
      enemy.pixelRangeAgainstFromCenter(unit) < unit.pixelRangeMax &&
      enemy.topSpeedChasing < unit.topSpeed
    }
    
    val shouldKite =
      unit.matchups.threatsViolent.exists(kiteable) ||
      unit.matchups.inFrames(24).threatsInRange.exists(kiteable)
    
    if (shouldKite) {
      Kite.delegate(unit)
    }
    else {
      Retreat.delegate(unit)
    }
  }
}
