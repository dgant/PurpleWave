package Micro.Actions.Combat.Techniques

import Mathematics.PurpleMath
import Micro.Actions.Combat.Techniques.Common.ActionTechnique
import Micro.Actions.Commands.Move
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Retreat extends ActionTechnique {
  
  // Go directly home.
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove
  }
  
  override val applicabilityBase: Double = 1.0
  
  override def applicabilitySelf(unit: FriendlyUnitInfo): Double = {
    if (unit.flying) return 0.0
    if (unit.zone == unit.agent.origin.zone) return 0.0
    1.0
  }
  
  override def applicabilityOther(unit: FriendlyUnitInfo, other: UnitInfo): Option[Double] = {
    if (other.isFriendly) return None
    if (!other.canAttack(unit)) return None

    // Retreat when we're against the wall
    val waypoint = unit.agent.nextWaypoint(unit.agent.origin)
    Some(
      PurpleMath.clampToOne(
        2 * other.pixelRangeAgainst(unit))
        / (other.pixelRangeAgainst(unit) + unit.pixelDistanceCenter(waypoint)))
  }
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    unit.agent.toTravel = Some(unit.agent.origin)
    // Some of these paths are wack
    // Path.delegate(unit)
    Move.delegate(unit)
  }
}
