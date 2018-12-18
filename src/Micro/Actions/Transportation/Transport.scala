package Micro.Actions.Transportation

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Smuggle
import Micro.Actions.Commands.Move
import Micro.Actions.Transportation.Caddy.BeAShuttle
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Transport extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.isTransport
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    Evacuate.consider(unit)
    // TODO: restore these once we have everything working
    if ( ! With.self.isProtoss) {
      DropOff.consider(unit)
      Pickup.consider(unit)
      Smuggle.consider(unit)
      Move.delegate(unit)
    }
    BeAShuttle.consider(unit)
  }
}
