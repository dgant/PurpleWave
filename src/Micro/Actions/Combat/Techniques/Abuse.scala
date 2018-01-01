package Micro.Actions.Combat.Techniques

import Micro.Actions.Combat.Tactics.Potshot
import Micro.Actions.Combat.Techniques.Common.{ActionTechnique, ShootAsSoonAsPossible}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Abuse extends ActionTechnique {
  
  // If we outrange and out-speed our enemies,
  // we can painlessly kill them as long as we maintain the gap and don't get cornered.
  // eg. Dragoons vs. slow Zealots
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.canAttack
    && unit.matchups.targets.nonEmpty
    && unit.matchups.threats.nonEmpty
  )
  
  override def applicabilityOther(unit: FriendlyUnitInfo, other: UnitInfo): Option[Double] = {
    if (other.isFriendly) return None
    if ( ! other.canAttack(unit)) return None
    if ( ! unit.canAttack(other)) return Some(0.0)
    
    val deltaSpeed = unit.topSpeed - other.topSpeed
    if (deltaSpeed <= 0) return Some(0.0)
  
    val deltaRange = unit.pixelRangeAgainstFromCenter(other) - other.pixelRangeAgainstFromCenter(unit)
    if (deltaRange <= 0) return Some(0.0)
    
    Some(1.0)
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    if (unit.matchups.framesOfSafetyDiffused > unit.unitClass.framesToTurnAndShootAndTurnBackAndAccelerate) {
      Potshot.delegate(unit)
      ShootAsSoonAsPossible.delegate(unit)
    }
    
    Avoid.delegate(unit)
  }
  
}
