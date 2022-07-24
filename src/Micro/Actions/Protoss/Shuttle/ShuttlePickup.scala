package Micro.Actions.Protoss.Shuttle

import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object ShuttlePickup extends Action {

  override def allowed(shuttle: FriendlyUnitInfo): Boolean = BeShuttle.allowed(shuttle) && ! shuttle.agent.passengers.forall(_.loaded)

  private def hailers(shuttle: FriendlyUnitInfo): Seq[FriendlyUnitInfo] = shuttle.agent.passengersPrioritized.filter(_.orderTarget.contains(shuttle))

  override protected def perform(shuttle: FriendlyUnitInfo): Unit = {
    hailers(shuttle).headOption.foreach(hailer => {
      shuttle.agent.toTravel = Some(hailer.pixel)
      Commander.rightClick(shuttle, hailer)
    })
  }
}
