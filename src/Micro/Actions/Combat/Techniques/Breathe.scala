package Micro.Actions.Combat.Techniques

import Mathematics.PurpleMath
import Micro.Actions.Combat.Techniques.Common.Activators.WeightedMean
import Micro.Actions.Combat.Techniques.Common.{ActionTechnique, AttackAsSoonAsPossible}
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Breathe extends ActionTechnique {
  
  // Shoot if we can; back off while recuperating cooldown
  // Example: Dragoon vs. Marines
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.canAttack
    && unit.matchups.targets.nonEmpty
    && unit.matchups.threats.nonEmpty
    && ! unit.unitClass.melee
    && ! unit.is(Protoss.Corsair) // Try to find a better generalizer; maybe cooldown vs. turn rate
    && ! unit.transport.exists(_.flying) // Transport cooldown means this just makes the unit flee
  )
  
  override val activator = new WeightedMean(this)
  
  override val applicabilityBase = 1.0
  
  override def applicabilityOther(unit: FriendlyUnitInfo, other: UnitInfo): Option[Double] = {
    if (other.isFriendly) return None
    if ( ! other.canAttack(unit)) return None
    
    val rangeOurs   = unit.pixelRangeAgainst(unit.matchups.targets.head)
    val rangeTheirs = other.pixelRangeAgainst(unit)
    val rangeDelta  = rangeOurs - rangeTheirs

    if (rangeDelta <= 0.0) return Some(0.0)

    // 2/19 speculative try
    if (other.orderTarget.contains(unit) && rangeDelta >= 0 && other.cooldownLeft < unit.cooldownMaxAirGround) return Some(1.0)
    
    val distanceRatio   = Math.min(1.0, unit.pixelRangeAgainst(other) / unit.pixelDistanceEdge(other))
    val cooldownOurs    = unit.cooldownMaxAgainst(unit.matchups.targets.head)
    val cooldownTheirs  = other.cooldownMaxAgainst(unit)
    val cooldownRatio   = cooldownOurs / cooldownTheirs
    
    Some(PurpleMath.clampToOne(cooldownRatio))
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    AttackAsSoonAsPossible.delegate(unit)
    if ( ! unit.readyForAttackOrder) {
      Avoid.delegate(unit)
    }
  }
}
