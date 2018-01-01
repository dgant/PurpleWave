package Micro.Actions.Combat.Techniques

import Micro.Actions.Combat.Techniques.Common.ActionTechnique
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Purr extends ActionTechnique {
  
  // Sit and get repaired.
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.unitClass.isMechanical
    && unit.totalHealth < unit.unitClass.maxTotalHealth
  )
  
  override def applicabilityOther(unit: FriendlyUnitInfo, other: UnitInfo): Option[Double] = {
    if (other.repairing && other.target.contains(unit)) return Some(1.0)
    
    None
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    Aim.delegate(unit)
  }
}
