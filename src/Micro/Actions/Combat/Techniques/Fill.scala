package Micro.Actions.Combat.Techniques

import Micro.Actions.Combat.Techniques.Common.ActionTechnique
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Fill extends ActionTechnique {
  
  // Pour through a choke to let teammates in.
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.canAttack
    && false
  )
  
  override def applicabilityOther(unit: FriendlyUnitInfo, other: UnitInfo): Option[Double] = {
    if (other.isFriendly) return None
    
    None
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    Charge.delegate(unit)
  }
}
