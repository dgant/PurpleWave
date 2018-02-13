package Micro.Actions.Combat.Techniques

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Micro.Actions.Combat.Attacking.Target
import Micro.Actions.Combat.Tactics.Potshot
import Micro.Actions.Combat.Techniques.Common.Activators.Min
import Micro.Actions.Combat.Techniques.Common.{ActionTechnique, PotshotAsSoonAsPossible}
import Micro.Actions.Commands.Attack
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Abuse extends ActionTechnique {
  
  // If we outrange and out-speed our enemies,
  // we can painlessly kill them as long as we maintain the gap and don't get cornered.
  // eg. Dragoons vs. slow Zealots
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.unitClass.ranged
    && unit.canAttack
    && unit.matchups.targets.nonEmpty
    && unit.matchups.threats.nonEmpty
  )
  
  override val activator = new Min(this)
  
  override def applicabilityOther(unit: FriendlyUnitInfo, other: UnitInfo): Option[Double] = {
    if (other.isFriendly) return None
    if ( ! other.canAttack(unit)) return None
    if ( ! unit.canAttack(other)) return None
    
    val framesOfSafetyAgainst = - unit.matchups.framesOfEntanglementPerThreatDiffused(other)
    if (framesOfSafetyAgainst < unit.unitClass.framesToTurnAndShootAndTurnBackAndAccelerate) return Some(0.0)
    
    val deltaSpeed = unit.topSpeed - other.topSpeed
    if (deltaSpeed <= 0) return Some(0.0)
  
    val deltaRange = unit.pixelRangeAgainst(other) - other.pixelRangeAgainst(unit)
    if (deltaRange <= 0) return Some(0.0)
    
    Some(1.0)
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    lazy val safeToShoot = unit.matchups.framesOfSafetyDiffused > unit.unitClass.framesToTurnAndShootAndTurnBackAndAccelerate
    lazy val lastChanceToShoot = unit.matchups.targetsInRange.isEmpty || unit.matchups.targets.forall(t => t.pixelDistanceEdge(unit) > unit.pixelRangeAgainst(t) - 32.0)
    if (unit.readyForAttackOrder && (safeToShoot || lastChanceToShoot)) {
      Potshot.delegate(unit)
      PotshotAsSoonAsPossible.delegate(unit)
      Target.delegate(unit)
      Attack.delegate(unit)
      if (unit.agent.toAttack.isEmpty) return
    }
    if (unit.matchups.framesOfSafetyDiffused < GameTime(0, 2)()) {
      Avoid.delegate(unit)
    }
  }
  
}
