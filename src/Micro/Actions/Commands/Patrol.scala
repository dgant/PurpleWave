package Micro.Actions.Commands

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Patrol extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.canAttack
    && unit.agent.toTravel.isDefined
  )
  
  override def perform(unit: FriendlyUnitInfo) {
    val pixelToPatrol = unit.agent.toTravel.get
    With.commander.patrol(unit, pixelToPatrol)
  }
}
