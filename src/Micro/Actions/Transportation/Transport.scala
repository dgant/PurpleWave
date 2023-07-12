package Micro.Actions.Transportation

import Micro.Actions.Action
import Micro.Actions.Protoss.Shuttle.BeShuttle
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Transport extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.isTransport
  
  override protected def perform(transport: FriendlyUnitInfo): Unit = {
    val toUnload = transport.agent.passengers.find(_.agent.wantsUnload)
    if (toUnload.isDefined && transport.tile.adjacent9.exists(_.walkable)) {
      Commander.unload(transport, toUnload.get)
      return
    }

    Evacuate(transport)
    BeShuttle(transport)
  }
}
