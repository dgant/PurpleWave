package Micro.Actions.Basic

import Micro.Actions.Action
import Micro.Actions.Commands.MoveHeuristically
import Micro.Behaviors.MovementProfiles
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Pardon extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.action.shovers.nonEmpty
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    unit.action.movementProfile = MovementProfiles.pardon
    MoveHeuristically.delegate(unit)
  }
}
