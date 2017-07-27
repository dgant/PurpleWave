package Micro.Actions.Combat.Decisionmaking

import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.{Cower, Sneak}
import Micro.Actions.Combat.Tactics.{BustBunker, Sally}
import Micro.Actions.Protoss.BeACarrier
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Fight extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMoveThisFrame || unit.readyForAttackOrder
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    Cower.consider(unit)
    Sneak.consider(unit)
    BeACarrier.consider(unit)
    Recover.consider(unit)
    Cast.consider(unit)
    BustBunker.consider(unit)
    Sally.consider(unit)
    FightOrFlight.consider(unit)
  }
}
