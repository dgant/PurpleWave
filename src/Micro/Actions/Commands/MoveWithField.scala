package Micro.Actions.Commands

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object MoveWithField extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove &&
    unit.agent.forces.nonEmpty
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    val forceTotal  = unit.agent.forces.values.reduce(_ + _)
    val forceNormal = forceTotal.normalize(85.0)
    val forcePoint  = forceNormal.toPoint
    val pixelToMove = unit.pixelCenter.add(forcePoint)
    unit.agent.movingTo = Some(pixelToMove)
    With.commander.move(unit, pixelToMove)
  }
}
