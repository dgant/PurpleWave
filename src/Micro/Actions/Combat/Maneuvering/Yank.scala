package Micro.Actions.Combat.Maneuvering

import Micro.Actions.Action
import Micro.Actions.Combat.Tactics.Potshot
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Yank extends Action {
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.agent.toLeash.exists(leash => {
      val distance = unit.pixelDistanceCenter(unit.agent.origin)
      distance > leash && distance < leash + 32.0 * 6
    })
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    Potshot.delegate(unit)
    Retreat.delegate(unit)
  }
}
