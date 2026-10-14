package Micro.Actions.Transportation

import Micro.Actions.Action
import Micro.Actions.Protoss.Shuttle.BeShuttle
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Transport extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.isTransport
  
  override protected def perform(transport: FriendlyUnitInfo): Unit = {
    val drop = transport.squad.collect { case mission: Tactic.Missions.MissionDrop => mission }
    drop.foreach(mission => {
      mission.accountForCargo(transport)
      mission.abandonUnsafePickups()
    })
    val passengers = transport.agent.passengers
    passengers.indices.foreach(i => {
      // Don't get stuck trying to unload a passenger that fails to land
      // and do save some frames for microing the transport itself
      if (i == transport.agent.run % (passengers.length + 1) && transport.tile.adjacent9.exists(_.walkable)
        && (passengers(i).agent.wantsUnload || drop.exists(_.requestsUnload(passengers(i))))) {
        Commander.unload(transport, passengers(i))
      }
    })

    Evacuate(transport)
    BeShuttle(transport)
  }
}
