package Micro.Actions.Combat.Techniques

import Micro.Actions.Combat.Techniques.Common.ActionTechnique
import Micro.Actions.Commands.Move
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Retreat extends ActionTechnique {
  
  // Go directly home.
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove
  }
  
  override val applicabilityBase: Double = 0.5
  
  override def applicabilitySelf(unit: FriendlyUnitInfo): Double = {
    if (unit.flying) return 0.0
    if (unit.zone == unit.agent.origin.zone) return 0.0
    1.0
  }
  
  override def applicabilityOther(unit: FriendlyUnitInfo, other: UnitInfo): Option[Double] = {
    if (other.isFriendly) {
      if (other.flying || unit.flying)
        return None
      if (other.pixelDistanceEdge(unit) > 2.0 * unit.unitClass.radialHypotenuse)
        return None
      return Some(0.0)
    }
      
    if ( ! other.canAttack(unit)) return None
    if (other.pixelDistanceCenter(unit.agent.origin) < unit.pixelDistanceCenter(unit.agent.origin))
      Some(0.0)
    else
      Some(1.0)
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    unit.agent.toTravel = Some(unit.agent.origin)
    Move.delegate(unit)
  }
}
