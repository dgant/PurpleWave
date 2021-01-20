package Micro.Actions.Transportation

import Micro.Actions.Action
import Micro.Actions.Transportation.Caddy.Shuttling
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Board extends Action {
  
  override protected def requiresReadiness: Boolean = false
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove &&
    unit.transport.isEmpty &&
    unit.agent.toBoard.isDefined
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val transport = unit.agent.toBoard.get
    
    unit.agent.toTravel = Some(transport.pixel)
    if (transport.pixelDistanceEdge(unit) < Shuttling.pickupRadius) {
      unit.hijack()
      Commander.rightClick(unit, transport)
    }
  }
}
