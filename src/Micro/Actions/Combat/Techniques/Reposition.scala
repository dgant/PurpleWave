package Micro.Actions.Combat.Techniques

import Debugging.Visualizations.ForceColors
import Micro.Actions.Combat.Targeting.Target
import Micro.Actions.Combat.Techniques.Common.Activators.One
import Micro.Actions.Combat.Techniques.Common.{ActionTechnique, AttackAsSoonAsPossible}
import Micro.Actions.Commands.{Attack, Gravitate, Move}
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
  
  override def applicabilitySelf(unit: FriendlyUnitInfo): Double = {
    - unit.matchups.vpfNet
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    AttackAsSoonAsPossible.delegate(unit)
    Target.delegate(unit)
    if (unit.agent.toAttack.isEmpty) return
    if ( ! unit.readyForMicro) return
  
    lazy val target         = unit.agent.toAttack.get
    lazy val shouldChase    = unit.pixelDistanceCenter(target) < unit.pixelDistanceCenter(target.projectFrames(3))
    lazy val shouldHugTank  = target.pixelRangeMin > 0 && target.canAttack(unit) && unit.pixelRangeAgainst(target) * 1.75 < target.pixelRangeAgainst(unit)
    lazy val shouldAvoid    = unit.matchups.threats.exists(t => (target != t || t.pixelRangeAgainst(unit) < unit.pixelRangeAgainst(t)))
  
    if (unit.readyForAttackOrder || ! unit.inRangeToAttack(target)) {
      Attack.delegate(unit)
    }
    else if (shouldChase || shouldHugTank) {
      val happy           = unit.matchups.vpfNet > 0
      val targetMagnitude = (if (happy) 2.0 else 1.0) * unit.pixelDistanceEdge(target) / unit.pixelRangeAgainst(target)
      val threatMagnitude = (if (happy) 1.0 else 2.0)
      val forceTarget     = Potential.unitAttraction(unit, target, targetMagnitude)
      val forceThreat     = Potential.threatsRepulsion(unit).normalize(threatMagnitude)
      val forceMobility   = Potential.mobilityAttraction(unit)
      val forceSpreading  = Potential.collisionRepulsion(unit)
      val forceRegrouping = Potential.teamAttraction(unit)
      unit.agent.forces.put(ForceColors.target,     forceTarget)
      unit.agent.forces.put(ForceColors.threat,     forceThreat)
      unit.agent.forces.put(ForceColors.mobility,   forceMobility)
      unit.agent.forces.put(ForceColors.spreading,  forceSpreading)
      unit.agent.forces.put(ForceColors.regrouping, forceRegrouping)
      Gravitate.consider(unit)
      Move.delegate(unit)
    }
    else if (shouldAvoid) {
      Avoid.consider(unit)
    }
  }
}
