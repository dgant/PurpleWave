package Micro.Actions.Basic

import Micro.Actions.Action
import Micro.Actions.Commands.MoveHeuristically
import Micro.Agency.MovementProfiles
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Pardon extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.shovers.nonEmpty
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    unit.agent.movementProfile = MovementProfiles.pardon
    MoveHeuristically.delegate(unit)
  }
}
