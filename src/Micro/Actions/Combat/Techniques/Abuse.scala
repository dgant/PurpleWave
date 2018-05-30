package Micro.Actions.Combat.Techniques

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Micro.Actions.Combat.Tactics.Potshot
import Micro.Actions.Combat.Techniques.Common.Activators.WeightedMin
import Micro.Actions.Combat.Techniques.Common.{ActionTechnique, PotshotAsSoonAsPossible}
import Micro.Actions.Commands.Attack
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Abuse extends ActionTechnique {
  
  // If we outrange and out-speed our enemies,
  // we can painlessly kill them as long as we maintain the gap and don't get cornered.
  // eg. Dragoons vs. slow Zealots
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.agent.canFight
    && unit.unitClass.ranged
    && unit.canAttack
    && unit.matchups.targets.nonEmpty
    && unit.matchups.threats.nonEmpty
  )
  
  override val activator = new WeightedMin(this)
  
  override def applicabilityOther(unit: FriendlyUnitInfo, other: UnitInfo): Option[Double] = {
    if (other.isFriendly) return None
    if ( ! other.canAttack(unit)) return None
    if ( ! unit.canAttack(other)) return None
    
    lazy val deltaThreats = other.matchups.threats.size - unit.matchups.threats.size
    lazy val deltaSpeed = unit.topSpeed - other.topSpeed
    lazy val canOle = (
      deltaSpeed == 0.0
        && deltaThreats > 0
        && ! other.inRangeToAttack(unit)
        && ! other.inRangeToAttack(unit, unit.unitClass.framesToTurnAndShootAndTurnBackAndAccelerate))
    
    if (deltaSpeed <= 0 && ! canOle) return Some(0.0)
  
    val deltaRange = unit.pixelRangeAgainst(other) - other.pixelRangeAgainst(unit)
    if (deltaRange <= 0) return Some(0.0)
    
    Some(1.0)
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    lazy val safeToShoot = unit.matchups.framesOfSafety > unit.unitClass.framesToTurnAndShootAndTurnBackAndAccelerate
    lazy val lastChanceToShoot = unit.matchups.targetsInRange.isEmpty || unit.matchups.targets.forall(t => t.pixelDistanceEdge(unit) > unit.pixelRangeAgainst(t) - 32.0)
    if (unit.readyForAttackOrder && (safeToShoot || lastChanceToShoot)) {
      Potshot.delegate(unit)
      PotshotAsSoonAsPossible.delegate(unit)
      Attack.delegate(unit)
      if (unit.agent.toAttack.isEmpty) return
    }
    if (unit.matchups.framesOfSafety < GameTime(0, 2)()) {
      Avoid.delegate(unit)
    }
  }
  
}
