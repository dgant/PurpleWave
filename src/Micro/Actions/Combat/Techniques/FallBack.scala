package Micro.Actions.Combat.Techniques

import Micro.Actions.Combat.Tactics.Potshot
import Micro.Actions.Combat.Techniques.Common.{ActionTechnique, Leave}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object FallBack extends ActionTechnique {
  
  // Shoot while retreating.
  // eg. Dragoons retreating from Vultures
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.canAttack
    && unit.unitClass.ranged
    && unit.matchups.targets.nonEmpty
  )
  
  override val applicabilityBase: Double = 0.5
  
  override def applicabilityOther(unit: FriendlyUnitInfo, other: UnitInfo): Option[Double] = {
    if (other.isFriendly) return None
    if ( ! other.canAttack(unit)) return None
    if ( ! unit.canAttack(other)) return Some(0.0)
    if (other.topSpeed <= 0.0) return None
    val speedRatio    = unit.topSpeedChasing / other.topSpeed
    val rangeRatio    = unit.pixelRangeAgainstFromEdge(other) / other.pixelRangeAgainstFromCenter(unit)
    val cooldownRatio = unit.cooldownMaxAgainst(other) / other.cooldownMaxAgainst(unit)
    val output        = speedRatio * rangeRatio * cooldownRatio
    Some(output)
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    Potshot.delegate(unit)
    Leave.delegate(unit)
  }
}
