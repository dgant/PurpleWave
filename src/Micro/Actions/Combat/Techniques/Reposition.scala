package Micro.Actions.Combat.Techniques

import Debugging.Visualizations.ForceColors
import Lifecycle.With
import Mathematics.Physics.Force
import Mathematics.PurpleMath
import Micro.Actions.Combat.Targeting.Target
import Micro.Actions.Combat.Techniques.Common.Activators.One
import Micro.Actions.Combat.Techniques.Common.{ActionTechnique, AttackAsSoonAsPossible}
import Micro.Actions.Commands.{Gravitate, Move}
import Micro.Decisions.Potential
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Reposition extends ActionTechnique {
  
  // Find a better place to stand while on cooldown.
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.canAttack
    && unit.unitClass.ranged
    && unit.matchups.targetsInRange.nonEmpty
    && unit.cooldownLeft > 0
  )
  
  override val activator = One
  
  override val applicabilityBase: Double = 0.0 // 0.8
  
  override def applicabilitySelf(unit: FriendlyUnitInfo): Double = {
    - unit.matchups.vpfNet
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    AttackAsSoonAsPossible.delegate(unit)
    if ( ! unit.readyForMicro) return
    
    Target.delegate(unit)
    
    var forceTarget     = new Force
    val forceThreat     = Potential.avoidThreats(unit)
    val forceSpreading  = Potential.preferSpreading(unit)
    val forceRegrouping = Potential.preferRegrouping(unit)
    val forceSpacing    = Potential.avoidCollision(unit)
    val resistancesTerrain = Potential.resistTerrain(unit)
  
    unit.agent.toAttack.foreach(target => {
      val targetMagnitude = PurpleMath.nanToOne((With.reaction.agencyAverage + unit.framesBeforeAttacking(target)) / unit.framesToBeReadyForAttackOrder.toDouble)
      forceTarget = Potential.unitAttraction(unit, target, targetMagnitude)
    })
    
    unit.agent.forces.put(ForceColors.target,     forceTarget)
    unit.agent.forces.put(ForceColors.threat,     forceThreat)
    unit.agent.forces.put(ForceColors.spreading,  forceSpreading)
    unit.agent.forces.put(ForceColors.regrouping, forceRegrouping)
    unit.agent.forces.put(ForceColors.spacing,    forceSpacing)
    unit.agent.resistances.put(ForceColors.mobility,  resistancesTerrain)
    Gravitate.consider(unit)
    Move.delegate(unit)
  }
}
