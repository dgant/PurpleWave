package Micro.Actions.Transportation

import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Transport extends Action {
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.isTransport
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    Evacuate.consider(unit)
    DropOff.consider(unit)
    Pickup.consider(unit)
  }
}
