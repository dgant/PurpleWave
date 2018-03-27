package Micro.Actions.Combat.Maneuvering

import Micro.Actions.Action
import Micro.Actions.Combat.Decisionmaking.Leave
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Yank extends Action {
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.agent.toLeash.isDefined
    && unit.pixelDistanceCenter(unit.agent.toLeash.get.pixelCenter) > unit.agent.toLeash.get.pixelRange
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    unit.agent.canFlee = true
    Leave.delegate(unit)
  }
}
