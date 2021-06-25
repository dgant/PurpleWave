package Micro.Actions.Transportation

import Micro.Actions.Action
import Micro.Actions.Protoss.Shuttle.BeShuttle
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Transport extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.isTransport
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    Evacuate.consider(unit)
    BeShuttle.consider(unit)
  }
}
