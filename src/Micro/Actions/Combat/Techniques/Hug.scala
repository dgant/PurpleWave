package Micro.Actions.Combat.Techniques

import Micro.Actions.Combat.Techniques.Common.ActionTechnique
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Hug extends ActionTechnique {

  // Sit on top of a Siege Tank.
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.canAttack
    && ! unit.flying
  )
  
  override def applicabilityOther(unit: FriendlyUnitInfo, other: UnitInfo): Option[Double] = {
    if (other.isFriendly) return None
    if (other.is(Terran.SiegeTankSieged)) return Some(1.0)
    if (other.is(Terran.SpiderMine)) return Some(1.0)
    Some(0.0)
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    Chase.delegate(unit)
  }
}
