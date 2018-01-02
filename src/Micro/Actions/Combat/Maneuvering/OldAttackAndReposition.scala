package Micro.Actions.Combat.Maneuvering

import Debugging.Visualizations.ForceColors
import Micro.Actions.Action
import Micro.Actions.Combat.Attacking.Target
import Micro.Actions.Commands.{Attack, Gravitate}
import Micro.Decisions.Potential
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object OldAttackAndReposition extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.unitClass.ranged
  )
  
  override def perform(unit: FriendlyUnitInfo) {
  
    Target.delegate(unit)
    if (unit.agent.toAttack.isEmpty) {
      return
    }
    
    lazy val target         = unit.agent.toAttack.get
    lazy val shouldChase    = unit.pixelDistanceFast(target) < unit.pixelDistanceFast(target.projectFrames(8))
    lazy val shouldHugTank  = target.pixelRangeMin > 0 && target.canAttack(unit) && unit.pixelDistanceFast(target) * 1.75 < target.pixelRangeAgainstFromCenter(unit)
    lazy val shouldAvoid    = unit.matchups.threats.exists(t => (target != t || t.pixelRangeAgainstFromCenter(unit) < unit.pixelRangeAgainstFromCenter(t)))
    
    if (unit.readyForAttackOrder || ! unit.inRangeToAttackFast(target)) {
      Attack.delegate(unit)
    }
    else if (shouldChase || shouldHugTank) {
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
    else if (shouldAvoid) {
      OldAvoid.consider(unit)
    }
  }
}
