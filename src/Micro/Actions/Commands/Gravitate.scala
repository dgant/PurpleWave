package Micro.Actions.Commands

import Lifecycle.With
import Micro.Actions.Action
import Micro.Decisions.Potential
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Gravitate extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove &&
    unit.agent.forces.nonEmpty
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    val forces          = unit.agent.forces.values
    val origin          = unit.pixelCenter
    val destination     = Potential.applyForcesForMoveOrder(forces, origin)
    unit.agent.movingTo = Some(destination)
    With.commander.move(unit, destination)
  }
}
