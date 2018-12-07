package Micro.Actions.Transportation.Caddy

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

object ShuttlePickup extends Action {
  override def allowed(shuttle: FriendlyUnitInfo): Boolean = BeAShuttle.allowed(shuttle)
  override protected def perform(shuttle: FriendlyUnitInfo): Unit = {
    val pickupCandidates = shuttle.teammates
      .view
      .filter(p =>
        p.is(Protoss.Reaver)
        && (p.scarabCount > 0 || p.training)) // TODO -- other units
      .flatMap(_.friendly)
      .filter(
        p => shuttle.canTransport(p)
        && p.pixelDistanceCenter(Shuttling.passengerDestination(p)) > Shuttling.dropoffRadius + 64)
      .toSeq
    val pickupCandidate = ByOption.maxBy(pickupCandidates)(c => Shuttling.pickupNeed(shuttle, c) / (1.0 + c.pixelDistanceSquared(shuttle)))
    pickupCandidate.foreach(hailer => {
      With.commander.rightClick(hailer, shuttle)
      With.commander.move(shuttle, hailer.pixelCenter)
    })
  }
}
