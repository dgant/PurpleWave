package Micro.Actions.Combat.Techniques

import Debugging.Visualizations.ForceColors
import Mathematics.Physics.ForceMath
import Mathematics.PurpleMath
import Micro.Actions.Combat.Targeting.Target
import Micro.Actions.Combat.Decisionmaking.Leave
import Micro.Actions.Combat.Techniques.Common.Activators.Activator
import Micro.Actions.Combat.Techniques.Common.{ActionTechnique, AttackAsSoonAsPossible}
import Micro.Actions.Commands.Gravitate
import Micro.Decisions.Potential
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

object Spread extends ActionTechnique {
  
  // Spread vs. splash damage.
  // Particularly intended for Mutalisks vs. Corsairs
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.canAttack
    && unit.flying
    && unit.matchups.allies.exists(isValidAlly)
    && unit.matchups.enemies.exists(isValidEnemy)
  )
  
  override protected val activator: Activator =
  
  protected def isValidAlly(ally: UnitInfo): Boolean = {
    ally.flying && ally.canAttack
  }
  
  protected def isValidEnemy(enemy: UnitInfo): Boolean = {
    enemy.canAttack && enemy.unitClass.attacksAir && enemy.unitClass.dealsRadialSplashDamage
  }
  
  override def applicabilityOther(unit: FriendlyUnitInfo, other: UnitInfo): Option[Double] = {
    if (other.isFriendly) return None
    if (isValidEnemy(other)) return Some(1.0)
    Some(0.0)
  }
  
  protected def targetAttractionMagnitude(unit: UnitInfo, target: UnitInfo): Double = {
    if (unit.framesToBeReadyForAttackOrder == 0)
      1.0
    else
      PurpleMath.clamp(unit.framesToGetInRange(target) / unit.framesToBeReadyForAttackOrder, 0.0, 1.0 )
  }
  
  protected def allyRepulsionMagnitude(splashRadius: Double): (UnitInfo, UnitInfo) => Double = {
    (self, ally) => {
      val denominator = splashRadius
      val numerator = - Math.max(0.0, denominator - self.pixelDistanceEdge(ally))
      numerator / denominator
    }
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    AttackAsSoonAsPossible.delegate(unit)
    if ( ! unit.readyForMicro) return
    
    Target.delegate(unit)
    
    // Not sure how this would happen, but just in case
    if (unit.agent.toAttack.isEmpty) {
      Leave.delegate(unit)
      return
    }
    
    val splashRadius: Double = ByOption.max(unit.matchups.threats.map(_.unitClass.airSplashRadius25.toDouble)).getOrElse(0.0)
    if (splashRadius == 0) return
    
    val allyRepulsion     = allyRepulsionMagnitude(splashRadius)
    val allies            = unit.matchups.allies.filter(isValidAlly)
    val forcesSpreading   = allies.map(Potential.unitAttraction(unit, _, allyRepulsion))
    val forceSpreading    = ForceMath.sum(forcesSpreading).normalize(forcesSpreading.map(_.lengthFast).max)
    val forceTarget       = Potential.unitAttraction(unit, unit.agent.toAttack.get, magnifier = targetAttractionMagnitude)
    
    unit.agent.forces.put(ForceColors.spreading, forceSpreading)
    unit.agent.forces.put(ForceColors.target, forceTarget)
    Gravitate.delegate(unit)
  }
}
