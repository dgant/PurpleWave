package Micro.Actions.Transportation

import Micro.Actions.Action
import Micro.Actions.Protoss.Shuttle.BeShuttle
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Transport extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.isTransport
  
  override protected def perform(transport: FriendlyUnitInfo): Unit = {
    val toUnload = transport.agent.passengers.filter(_.agent.wantsUnload)
    transport.agent.passengers.indices.foreach(i => {
      // Don't get stuck trying to unload a passenger that fails to land
      // and do save some frames for microing the transport itself
      if (i == transport.agent.run % (transport.agent.passengers.length + 1) && transport.tile.adjacent9.exists(_.walkable)) {
        Commander.unload(transport, transport.agent.passengers(i))
      }
    })

    Evacuate(transport)
    BeShuttle(transport)
  }
}
