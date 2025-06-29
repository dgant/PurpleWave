package Micro.Actions.Terran

import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object DemandRepair extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = false
  
  override def perform(unit: FriendlyUnitInfo): Unit = {
  }
}
