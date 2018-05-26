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
  )
  
  override val activator = One
  
  override val applicabilityBase: Double = 0.8
  
  override def applicabilitySelf(unit: FriendlyUnitInfo): Double = {
    - unit.matchups.vpfNet
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    AttackAsSoonAsPossible.delegate(unit)
    if ( ! unit.readyForMicro) return
    
    Target.delegate(unit)
    
    var forceTarget     = new Force
    val forceThreat     = Potential.threatsRepulsion(unit)
    val forceMobility   = Potential.mobilityAttraction(unit)
    val forceSpacing    = Potential.collisionRepulsion(unit)
    val forceSpreading  = Potential.splashRepulsion(unit)
    val forceRegrouping = Potential.teamAttraction(unit)
  
    unit.agent.toAttack.foreach(target => {
      val targetMagnitude = PurpleMath.nanToOne((With.reaction.agencyAverage + unit.framesBeforeAttacking(target)) / unit.framesToBeReadyForAttackOrder)
      forceTarget = Potential.unitAttraction(unit, target, targetMagnitude)
    })
    
    unit.agent.forces.put(ForceColors.target,     forceTarget)
    unit.agent.forces.put(ForceColors.threat,     forceThreat)
    unit.agent.forces.put(ForceColors.mobility,   forceMobility)
    unit.agent.forces.put(ForceColors.spacing,    forceSpacing)
    unit.agent.forces.put(ForceColors.spreading,  forceSpreading)
    unit.agent.forces.put(ForceColors.regrouping, forceRegrouping)
    Gravitate.consider(unit)
    Move.delegate(unit)
  }
}
