package Micro.Actions.Transportation

import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Smuggle
import Micro.Actions.Commands.Move
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Transport extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.isTransport
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    Evacuate.consider(unit)
    DropOff.consider(unit)
    Pickup.consider(unit)
    val fast = unit.player.hasUpgrade(Protoss.ShuttleSpeed)
    Smuggle.consider(unit)
    Move.delegate(unit)
  }
}
