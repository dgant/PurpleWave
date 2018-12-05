package Micro.Actions.Transportation.Caddy

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Techniques.Avoid
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

object BeAShuttle extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.is(Protoss.Shuttle)

  override protected def perform(shuttle: FriendlyUnitInfo): Unit = {
    if (shuttle.battle.isDefined) {
      ShuttleDropoff.consider(shuttle)
      ShuttlePickup.consider(shuttle)
    } else {
      ShuttlePickup.consider(shuttle)
      ShuttleDropoff.consider(shuttle)
    }

    if (shuttle.readyForMicro) {
      val passenger = Shuttling.passengerTarget(shuttle).map(_.pixelCenter)
      if (passenger.isDefined) {
        shuttle.agent.toTravel = passenger
      } else if (shuttle.matchups.framesOfSafety < With.reaction.agencyMax + shuttle.unitClass.framesToTurn180) {
        Avoid.consider(shuttle)
      } else {
        shuttle.agent.toTravel =
          ByOption.minBy(With.units.ours.view.filter(_.is(Protoss.RoboticsFacility)))(_.pixelDistanceCenter(shuttle))
          .map(_.pixelCenter)
          .orElse(shuttle.agent.toTravel)
      }
    }
  }
}
