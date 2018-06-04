package Micro.Actions.Combat.Techniques

import Debugging.Visualizations.ForceColors
import Mathematics.Physics.Force
import Mathematics.PurpleMath
import Micro.Actions.Combat.Targeting.Target
import Micro.Actions.Combat.Techniques.Common.Activators.{Activator, WeightedMax}
import Micro.Actions.Combat.Techniques.Common.{ActionTechnique, AttackAsSoonAsPossible}
import Micro.Actions.Commands.{Gravitate, Move}
import Micro.Decisions.Potential
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

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
  
  override val activator: Activator = new WeightedMax(this)
  
  protected def isValidAlly(ally: UnitInfo): Boolean = {
    ally.flying && ally.canAttack
  }
  
  protected def isValidEnemy(enemy: UnitInfo): Boolean = {
    if (enemy.isAny(
      Terran.ScienceVessel,
      Protoss.DarkArchon,
      Protoss.HighTemplar)) return true
    
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
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    AttackAsSoonAsPossible.delegate(unit)
    if ( ! unit.readyForMicro) return
  
    Target.delegate(unit)
    
    val forceSpreading    = Potential.preferSpreading(unit)
    var forceTarget       = new Force
    
    unit.agent.toAttack.foreach(target => {
      forceTarget = Potential.unitAttraction(unit, unit.agent.toAttack.get, 1.0)
    })
    
    unit.agent.forces.put(ForceColors.spacing, forceSpreading)
    unit.agent.forces.put(ForceColors.target, forceTarget)
    Gravitate.delegate(unit)
    Move.delegate(unit)
  }
}
