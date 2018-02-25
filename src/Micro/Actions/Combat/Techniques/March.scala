package Micro.Actions.Combat.Techniques

import Debugging.Visualizations.ForceColors
import Micro.Actions.Combat.Attacking.Target
import Micro.Actions.Combat.Techniques.Common.ActionTechnique
import Micro.Actions.Commands.Gravitate
import Micro.Decisions.Potential
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

object March extends ActionTechnique {
  
  // Advance while in formation
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canAttack
    && ! unit.flying
    && unit.canMove
    && unit.matchups.targetsInRange.isEmpty
  )
  
  override def applicabilitySelf(unit: FriendlyUnitInfo): Double = {
    if (unit.flying) return 0.0
    if (unit.matchups.allies.exists(_.matchups.threatsInRange.nonEmpty)) return 0.0
    val collisionDistance = ByOption.min(unit.matchups.allies.filterNot(_.flying).map(_.pixelDistanceEdge(unit)))
    collisionDistance.map(d => Math.min(1.0, 32.0 / (1.0 + d))).getOrElse(0.0)
  }
  
  override def applicabilityOther(unit: FriendlyUnitInfo, other: UnitInfo): Option[Double] = {
    if (other.isFriendly) return None
    if ( ! unit.canAttack(other)) return None
    if ( ! other.canAttack(unit)) return None
  
    if (other.speedApproaching(unit.pixelCenter) > 0) return Some(1.0)
    if (unit.topSpeedChasing > other.topSpeed) return Some(0.0)
    Some(24.0 / (1.0 + unit.framesToGetInRange(other)))
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    Target.delegate(unit)
    if (unit.agent.toAttack.isEmpty) {
      return
    }
    val target = unit.agent.toAttack.get
    val forceTarget     = Potential.unitAttraction(unit, target, 1.0)
    val forceMobility   = Potential.mobilityAttraction(unit)
    val forceSpreading  = Potential.collisionRepulsion(unit)
    val forceRegrouping = Potential.teamAttraction(unit)
  
    unit.agent.forces.put(ForceColors.target,     forceTarget)
    unit.agent.forces.put(ForceColors.mobility,   forceMobility)
    unit.agent.forces.put(ForceColors.spreading,  forceSpreading)
    unit.agent.forces.put(ForceColors.regrouping, forceRegrouping)
    Gravitate.delegate(unit)
  }
}
