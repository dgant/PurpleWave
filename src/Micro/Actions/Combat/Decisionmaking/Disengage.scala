package Micro.Actions.Combat.Decisionmaking

import Micro.Actions.Action
import Micro.Actions.Combat.Attacking.Potshot
import Micro.Actions.Combat.Maneuvering.{Kite, Retreat}
import Planning.Yolo
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Disengage extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.canFlee   &&
    unit.canMove          &&
    ! Yolo.active
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    
    // Experimental. This negates all the reasoning we do in Retreat to decide if we want to potshot.
    Potshot.delegate(unit)
    
    if ( ! unit.readyForMicro) return
    
    def kiteable(enemy: UnitInfo): Boolean = {
      enemy.pixelRangeAgainstFromCenter(unit) < unit.pixelRangeMax &&
      enemy.topSpeedChasing < unit.topSpeed
    }
    
    val shouldKite =
      unit.matchups.threatsViolent.exists(kiteable) ||
      unit.matchups.ifAt(24).threatsInRange.exists(kiteable)
    
    if (shouldKite) {
      Kite.consider(unit)
    }
    else {
      Retreat.delegate(unit)
    }
  }
}
