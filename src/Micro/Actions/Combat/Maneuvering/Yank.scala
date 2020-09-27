package Micro.Actions.Combat.Maneuvering

import Micro.Actions.Action
import Micro.Actions.Combat.Tactics.Potshot
import Micro.Actions.Commands.Move
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Yank extends Action {
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.agent.toLeash.exists(leash => {
      val distance = unit.pixelDistanceCenter(leash.pixelCenter)
      val range = leash.pixelRange
      distance > range && distance < range + 32.0 * 6
    })
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    Potshot.delegate(unit)
    unit.agent.canFlee = true
    unit.agent.toTravel = Some(unit.agent.toLeash.get.pixelCenter)
    Move.delegate(unit)
  }
}
