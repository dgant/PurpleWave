package Micro.Actions.Commands

import Lifecycle.With
import Mathematics.Physics.ForceMath
import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Gravitate extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove &&
    unit.agent.forces.nonEmpty
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    val forces          = unit.agent.forces.values
    val origin          = unit.pixelCenter
    val forceTotal      = ForceMath.sum(forces)
    val forceNormal     = forceTotal.normalize(85.0)
    val forcePoint      = forceNormal.toPoint
    val destination     = origin.add(forcePoint)
    With.commander.move(unit, destination)
  }
}
