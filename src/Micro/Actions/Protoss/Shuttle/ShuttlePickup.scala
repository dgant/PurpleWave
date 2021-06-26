package Micro.Actions.Protoss.Shuttle

import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object ShuttlePickup extends Action {

  override def allowed(shuttle: FriendlyUnitInfo): Boolean = BeShuttle.allowed(shuttle) && ! shuttle.agent.passengers.forall(_.loaded)

  private def hailers(shuttle: FriendlyUnitInfo): Seq[FriendlyUnitInfo] = shuttle.agent.prioritizedPassengers.filter(_.orderTarget.contains(shuttle))

  override protected def perform(shuttle: FriendlyUnitInfo): Unit = {
    hailers(shuttle).headOption.map(_.pixel).foreach(p => {
      shuttle.agent.toTravel = Some(p)
      Commander.move(shuttle)
    })
  }
}
