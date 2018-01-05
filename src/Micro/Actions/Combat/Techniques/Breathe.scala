package Micro.Actions.Combat.Techniques

import Micro.Actions.Combat.Techniques.Common.{ActionTechnique, AttackAsSoonAsPossible, Leave}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Breathe extends ActionTechnique {
  
  // Shoot if we can; back off while recuperating cooldown
  // Example: Dragoon vs. Marines
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.canAttack
    && unit.matchups.targets.nonEmpty
    && unit.matchups.threats.nonEmpty
  )
  
  override val activator = RMS
  
  override val applicabilityBase = 1.0
  
  override def applicabilityOther(unit: FriendlyUnitInfo, other: UnitInfo): Option[Double] = {
    if (other.isFriendly) return None
    if ( ! other.canAttack(unit)) return None
    
    val distanceOurs    = unit.pixelRangeMax
    val distanceTheirs  = other.pixelDistanceFast(unit)
    val distanceDelta   = distanceOurs - distanceTheirs
    
    val rangeOurs       = unit.pixelRangeAgainstFromEdge(unit.matchups.targets.head)
    val rangeTheirs     = other.pixelRangeAgainstFromEdge(unit)
    val rangeDelta      = rangeOurs - rangeTheirs
    
    // Does this even make sense?
    val safetyMargin    = rangeDelta + distanceDelta
    
    // This is a good candidate for weighing relevance.
    if (safetyMargin <= 0.0) return Some(0.0)
    
    val cooldownOurs    = unit.cooldownMaxAgainst(unit.matchups.targets.head)
    val cooldownTheirs  = other.cooldownMaxAgainst(unit)
    val cooldownRatio   = cooldownOurs / cooldownTheirs
    
    Some(cooldownRatio)
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    AttackAsSoonAsPossible.delegate(unit)
    if ( ! unit.readyForAttackOrder) {
      Leave.delegate(unit)
    }
  }
}
