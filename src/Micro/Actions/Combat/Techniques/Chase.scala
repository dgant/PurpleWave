package Micro.Actions.Combat.Techniques

import Lifecycle.With
import Micro.Actions.Combat.Targeting.Target
import Micro.Actions.Combat.Techniques.Common.ActionTechnique
import Micro.Actions.Commands.{Attack, Move}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Chase extends ActionTechnique {
  
  // Chase down an enemy that's running away
  // eg. Corsairs vs. Mutalisks
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.canAttack
    && ! unit.is(Terran.SCV) // SCVs are just really bad at this
  )
  
  override def applicabilitySelf(unit: FriendlyUnitInfo): Double = {
    if (unit.is(Zerg.Lurker)) return 0.0
    if (unit.unitClass.minStop > 0) return 0.0
    1.0
  }
  
  override def applicabilityOther(unit: FriendlyUnitInfo, other: UnitInfo): Option[Double] = {
    if (other.isFriendly) return None
    
    lazy val weCanAttack    = unit.canAttack(other)
    lazy val theyCanAttack  = other.canAttack(unit)
    lazy val rangeUs        = unit.pixelRangeAgainst(other)
    lazy val rangeEnemy     = other.pixelRangeAgainst(unit)
  
    if ( ! weCanAttack  && ! theyCanAttack)                   return None
    if (theyCanAttack   && ! weCanAttack)                     return Some(0.0)
    if (weCanAttack     && ! theyCanAttack)                   return Some(1.0)
    if (rangeUs < rangeEnemy && ! other.is(Protoss.Corsair))  return Some(1.0) // Corsairs attack too quickly to make use of increased range
    if (other.isBeingViolent)                                 return Some(0.0)
    Some(1.0)
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    Target.delegate(unit)
    if (unit.agent.toAttack.isEmpty) return

    val target = unit.agent.toAttack.get
    val readyToAttack = unit.readyForAttackOrder
    val canEscape = target.canMove
    val nearEscaping = target.matchups.framesOfEntanglementWith(unit) < With.reaction.agencyAverage + unit.unitClass.accelerationFrames
    val speedEscaping = - unit.speedApproachingEachOther(target)

    def attack() { Attack.delegate(unit) }
    def pursue(){
      val targetProjected = target.projectFrames(unit.framesToBeReadyForAttackOrder)
      val distanceToTravel = Math.max(
        unit.pixelDistanceCenter(targetProjected),
        unit.unitClass.haltPixels + unit.topSpeed * With.reaction.agencyMax)
      val targetPixel = unit.pixelCenter.project(targetProjected, distanceToTravel)
      unit.agent.toTravel = Some(targetPixel)
      Move.delegate(unit)
    }

    if (readyToAttack && (unit.unitClass.accelerationFrames <= 1 || unit.is(Protoss.DarkTemplar))) {
      attack()
    }

    // Don't get TOO fancy
    if (readyToAttack && unit.pixelDistanceEdge(target) < unit.pixelRangeAgainst(target) / 3) {
      attack()
    }

    if (canEscape && speedEscaping > 0 && (nearEscaping || unit.is(Protoss.Corsair))) {
      pursue()
    }

    if (readyToAttack) {
      attack()
    }

    if (nearEscaping || speedEscaping > 0) {
      pursue()
    }

    attack()
  }
}
