package Micro.Actions.Basic

import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Pardon extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.shovers.nonEmpty
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    // TODO: Get out of the way!
  }
}
