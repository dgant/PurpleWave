package Micro.Actions.Combat.Maneuvering

import Micro.Actions.Action
import Micro.Actions.Combat.Attacking.Potshot
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Kite extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMoveThisFrame
    && unit.matchups.targets.nonEmpty
    && unit.matchups.threats.nonEmpty
    && unit.pixelRangeMax > 32 * 3.0
  )
  
  override def perform(unit: FriendlyUnitInfo) {
  
    lazy val dominating = unit.matchups.threats.forall(threat =>
      unit.topSpeed       > threat.topSpeed &&
      unit.pixelRangeMax  > threat.pixelRangeMax)
    
    if (unit.readyForAttackOrder) {
      if (dominating) {
        // Before shooting, make sure we have ample space.
        // Also, don't close distance unless we are faster.
        val sufficientSpace = unit.matchups.threatsViolent.forall(_.framesBeforeAttacking(unit) > unit.unitClass.stopFrames + unit.unitClass.minStop)
        if (sufficientSpace) {
          Potshot.consider(unit)
        } else {
          Avoid.delegate(unit)
        }
      } else {
        Potshot.consider(unit)
      }
    }
    
    if (dominating) {
      KiteMove.delegate(unit)
    }
    else {
      Retreat.delegate(unit)
    }
  }
}
