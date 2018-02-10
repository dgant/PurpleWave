package Micro.Actions.Combat.Techniques

import Lifecycle.With
import Micro.Actions.Combat.Attacking.Target
import Micro.Actions.Combat.Techniques.Common.ActionTechnique
import Micro.Actions.Commands.AttackMove
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Brawl extends ActionTechnique {
  
  // In close-quarters fights against other melee units,
  // prefer non-targeted commands to avoid glitching.
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.matchups.targets.exists(_.unitClass.melee)
    && unit.matchups.threats.exists(_.unitClass.melee)
  )
  
  override val activator = RMS
  
  override def applicabilitySelf(unit: FriendlyUnitInfo): Double = {
    if (unit.unitClass.melee) 1.0 else 0.0
  }
  
  override def applicabilityOther(unit: FriendlyUnitInfo, other: UnitInfo): Option[Double] = {
    if (other.isFriendly) return None
    if (other.flying) return None
    if ( ! other.canAttack(unit)) return None
    if ( ! unit.canAttack(other)) return None
    if (other.unitClass.ranged) return Some(0.0)
    
    val framesBeforeContact = unit.pixelDistanceEdge(other) / (unit.topSpeed + other.topSpeed)
    val framesBeforeReacting = With.reaction.agencyMax + unit.unitClass.turn180Frames
    Some(2.0 * framesBeforeReacting / (1.0 + framesBeforeContact))
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    if (unit.unitClass.melee)
    Target.delegate(unit)
    unit.agent.toTravel = unit.agent.toAttack.map(_.pixelCenter)
    AttackMove.delegate(unit)
  }
}
