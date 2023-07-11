package Micro.Actions.Protoss.Shuttle

import Lifecycle.With
import Mathematics.Maff
import Micro.Actions.Action
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object ShuttleAdoptPassenger extends Action {

  override def allowed(shuttle: FriendlyUnitInfo): Boolean = BeShuttle.allowed(shuttle)

  override protected def perform(shuttle: FriendlyUnitInfo): Unit = {
    val pickupCandidates =
      Maff.orElse(
        shuttle.alliesSquad.view.filter(shouldAdopt(shuttle, _)),
        shuttle.alliesBattle.view.filter(shouldAdopt(shuttle, _)),
        With.units.ours.filter(shouldAdopt(shuttle, _)))
    Maff.maxBy(pickupCandidates)(c => pickupNeed(shuttle, c) / (1.0 + c.pixelDistanceSquared(shuttle))).foreach(shuttle.agent.addPassenger)
  }

  protected def shouldAdopt(shuttle: FriendlyUnitInfo, passenger: FriendlyUnitInfo): Boolean = (
    // Can we adopt this passenger?
    shuttle.canTransport(passenger)
      && passenger.transport.isEmpty
      && passenger.agent.ride.forall(shuttle==)
      && passenger.unitClass.spaceRequired + shuttle.agent.passengers.view.map(_.unitClass.spaceRequired).sum <= shuttle.unitClass.spaceProvided
    // Do we want to adopt this passenger?
      && (passenger.isAny(Protoss.Reaver, Protoss.HighTemplar) || passenger.matchups.targetedByScarab)
      && (shuttle.agent.passengers.isEmpty || shuttle.agent.passengers.headOption.map(_.squad).getOrElse(shuttle.squad).forall(passenger.squad.contains))
    )

  protected def pickupNeed(shuttle: FriendlyUnitInfo, hailer: FriendlyUnitInfo): Double = {
    val endangered = hailer.matchups.framesOfSafety < shuttle.framesToTravelTo(hailer.pixel) + 2 * shuttle.unitClass.framesToTurn180
    val sojourning = hailer.agent.toTravel.exists(_.pixelDistance(hailer.pixel) > 32.0 * 20)
    var output = 1.0
    if (Protoss.Reaver(hailer))                         output *= 1000
    if (hailer.matchups.targetedByScarab)               output *= 100
    if (shuttle.squad.exists(hailer.squad.contains))    output *= 10
    if (shuttle.battle.exists(hailer.battle.contains))  output *= 5
    if (endangered)                                     output *= 10
    if (sojourning)                                     output *= 2
    output
  }
}
