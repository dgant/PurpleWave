package Micro.Actions.Combat.Techniques

import Micro.Actions.Combat.Attacking.Target
import Micro.Actions.Combat.Techniques.Common.ActionTechnique
import Micro.Actions.Commands.{Attack, Move}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Chase extends ActionTechnique {
  
  // Chase down an enemy that's running away
  // eg. Corsairs vs. Mutalisks
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.canAttack
  )
  
  override def applicabilitySelf(unit: FriendlyUnitInfo): Double = {
    if (unit.unitClass.minStop > 0) return 0.0
    if (unit.unitClass.attackAnimationFrames > 2) return 0.0
    1.0
  }
  
  override def applicabilityOther(unit: FriendlyUnitInfo, other: UnitInfo): Option[Double] = {
    if (other.isFriendly) return None
    
    val weCanAttack   = unit.canAttack(other)
    val theyCanAttack = other.canAttack(unit)
    
    if (theyCanAttack   && ! weCanAttack)   return Some(0.0)
    if (weCanAttack     && ! theyCanAttack) return Some(1.0)
    if ( ! weCanAttack  && ! theyCanAttack) return None
    
    // TODO: Really didn't think about this too carefully
    val weAreOutRanged = unit.pixelRangeAgainst(other) < other.pixelRangeAgainst(unit)
    if (weAreOutRanged) return Some(1.0)
    if (other.isBeingViolent) return Some(0.0)
    
    Some(1.0)
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    Target.delegate(unit)
    if (unit.readyForAttackOrder) {
      Attack.delegate(unit)
    }
  
    // TODO: Queue up a move order ASAP; we can't wait for latency
    val target = unit.agent.toAttack
    if (target.exists(_.speedApproachingPixel(unit.pixelCenter) < 0.0)) {
      unit.agent.toTravel = Some(unit.pixelCenter.project(
        target.get.pixelCenter,
        unit.pixelDistanceEdge(target.get) + 48.0))
      Move.delegate(unit)
    }
  }
}
