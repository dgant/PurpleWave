package Micro.Actions.Transportation.Caddy

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Support
import Micro.Actions.Combat.Techniques.Avoid
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object BeAShuttle extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.is(Protoss.Shuttle)

  override protected def perform(shuttle: FriendlyUnitInfo): Unit = {
    ShuttleDropoff.consider(shuttle)
    ShuttlePickup.consider(shuttle)
    Support.consider(shuttle)
    ShuttleQueue.consider(shuttle)
    if (shuttle.matchups.framesOfSafety < With.reaction.agencyMax + shuttle.unitClass.framesToTurn180) {
      Avoid.consider(shuttle)
    }
  }
}
