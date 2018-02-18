package Micro.Actions.Combat.Techniques

import Lifecycle.With
import Micro.Actions.Combat.Attacking.Target
import Micro.Actions.Combat.Techniques.Common.ActionTechnique
import Micro.Actions.Commands.{Attack, Move}
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Chase extends ActionTechnique {
  
  // Chase down an enemy that's running away
  // eg. Corsairs vs. Mutalisks
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.canAttack
  )
  
  override def applicabilitySelf(unit: FriendlyUnitInfo): Double = {
    if (unit.is(Zerg.Lurker)) return 0.0
    if (unit.unitClass.minStop > 0) return 0.0
    1.0
  }
  
  override def applicabilityOther(unit: FriendlyUnitInfo, other: UnitInfo): Option[Double] = {
    if (other.isFriendly) return None
    
    lazy val weCanAttack   = unit.canAttack(other)
    lazy val theyCanAttack = other.canAttack(unit)
    lazy val rangeUs     = unit.pixelRangeAgainst(other)
    lazy val rangeEnemy  = other.pixelRangeAgainst(unit)
  
    if ( ! weCanAttack  && ! theyCanAttack) return None
    if (theyCanAttack   && ! weCanAttack)   return Some(0.0)
    if (weCanAttack     && ! theyCanAttack) return Some(1.0)
    if (unit.flying     && other.flying)    return Some(1.0)
    if (rangeUs < rangeEnemy)               return Some(1.0)
    if (other.isBeingViolent)               return Some(0.0)
    
    Some(1.0)
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    Target.delegate(unit)
    if (unit.agent.toAttack.isEmpty) return
    
    if (unit.readyForAttackOrder) {
      Attack.delegate(unit)
    }
    
    // Chase the target down
    // TODO: Queue up a move order ASAP; we can't wait for latency
    val target = unit.agent.toAttack.get
    if (target.speedApproachingPixel(unit.pixelCenter) <= 0.0) {
      val targetProjected = target.projectFrames(unit.framesToBeReadyForAttackOrder)
      val distanceToTravel = Math.max(
        unit.pixelDistanceCenter(targetProjected),
        unit.unitClass.haltPixels + unit.topSpeed * With.reaction.agencyMax)
      val targetPixel = unit.pixelCenter.project(targetProjected, distanceToTravel)
      unit.agent.toTravel = Some(targetPixel)
      Move.delegate(unit)
    }
  }
}
