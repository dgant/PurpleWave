package Micro.Actions.Combat.Techniques

import Micro.Actions.Combat.Techniques.Common.ActionTechnique
import Micro.Actions.Commands.Move
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Retreat extends ActionTechnique {
  
  // Go directly home.
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove
  }
  
  override def applicabilitySelf(unit: FriendlyUnitInfo): Double = {
    if (unit.flying) return 0.0
    if (unit.zone == unit.agent.origin.zone) return 0.0
    0.5
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    unit.agent.toTravel = Some(unit.agent.origin)
    Move.delegate(unit)
  }
}
