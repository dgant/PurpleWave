package Micro.Actions.Combat.Techniques

import Micro.Actions.Combat.Techniques.Common.ActionTechnique
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Isolate extends ActionTechnique {

  // Back into a corner to fight against multiple melee enemies.
  
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
    // TODO
  }
}
