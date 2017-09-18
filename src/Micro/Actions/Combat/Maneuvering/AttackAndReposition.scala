package Micro.Actions.Combat.Maneuvering

import Debugging.Visualizations.ForceColors
import Micro.Actions.Action
import Micro.Actions.Combat.Attacking.Target
import Micro.Actions.Commands.{Attack, Gravitate}
import Micro.Decisions.Potential
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object AttackAndReposition extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove && unit.ranged
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    
    if (unit.matchups.targets.isEmpty) {
      Avoid.consider(unit)
      return
    }
  
    Target.delegate(unit)
    if (unit.agent.toAttack.isEmpty) {
      return
    }
    
    val target = unit.agent.toAttack.get
    if (unit.readyForAttackOrder || ! unit.inRangeToAttackFast(target)) {
      Attack.delegate(unit)
      return
    }
    
    lazy val targetFleeing      = unit.pixelDistanceFast(target) < unit.pixelDistanceFast(target.projectFrames(8))
    lazy val threatAnnoying     = unit.matchups.threatsInRange.exists(t => target != t || t.pixelRangeAgainstFromCenter(unit) < unit.pixelRangeAgainstFromCenter(t))
    lazy val threatApproaching  = unit.matchups.threats.exists(threat => threat.canMove && threat.hasBeenViolentInLastTwoSeconds)
    lazy val shouldAvoid        = threatAnnoying || threatApproaching
    lazy val shouldChase        = targetFleeing
    lazy val shouldHug          = target.pixelRangeMin > 0 && target.canAttack(unit) && unit.pixelDistanceFast(target) * 1.75 < target.pixelRangeAgainstFromCenter(unit)
    
    if (shouldAvoid || shouldChase || shouldHug) {
      val happy           = unit.matchups.vpfNetDiffused > 0
      val targetMagnitude = (if (happy) 2.0 else 1.0) * unit.pixelDistanceFast(target) / unit.pixelRangeAgainstFromCenter(target)
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
    }
  }
}
