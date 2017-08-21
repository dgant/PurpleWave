package Micro.Actions.Transportation

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Board extends Action {
  
  override protected def requiresReadiness: Boolean = false
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove &&
    unit.transport.isEmpty &&
    unit.agent.toBoard.isDefined
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val transport = unit.agent.toBoard.get
    
    unit.agent.toTravel = Some(transport.pixelCenter)
    if (transport.pixelDistanceFast(unit) < With.configuration.pickupRadiusPixels) {
      With.commander.hijack(unit)
      With.commander.rightClick(unit, transport)
    }
  }
}
