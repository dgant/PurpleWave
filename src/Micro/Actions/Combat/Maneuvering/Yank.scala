package Micro.Actions.Combat.Maneuvering

import Micro.Actions.Action
import Micro.Actions.Commands.Move
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Yank extends Action {
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.agent.toLeash.isDefined
    && {
      val distance = unit.pixelDistanceCenter(unit.agent.toLeash.get.pixelCenter)
      val range = unit.agent.toLeash.get.pixelRange
      distance > range && distance < range + 32.0 * 6
    }
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    unit.agent.canFlee = true
    unit.agent.toTravel = Some(unit.agent.toLeash.get.pixelCenter)
    Move.delegate(unit)
  }
}
