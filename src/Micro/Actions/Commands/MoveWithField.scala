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
    val totalForce = unit.agent.forces.values.reduce(_ + _)
    val pixelToMove = unit.pixelCenter.project(unit.pixelCenter.add(totalForce.x.toInt, totalForce.y.toInt), 85.0)
    unit.agent.movingTo = Some(pixelToMove)
    With.commander.move(unit, pixelToMove)
  }
}
