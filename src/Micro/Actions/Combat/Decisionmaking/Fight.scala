package Micro.Actions.Combat.Decisionmaking

import Micro.Actions.Action
import Micro.Actions.Combat.Attacking.{Pillage, Pursue}
import Micro.Actions.Combat.Maneuvering.{Cower, Sneak}
import Micro.Actions.Combat.Specialized.{CarrierLeash, Recover}
import Micro.Actions.Combat.Tactics.{BustBunker, ProtectTheWeak}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Fight extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMoveThisFrame || unit.readyForAttackOrder
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    Cower.consider(unit)
    Sneak.consider(unit)
    CarrierLeash.consider(unit)
    Cast.consider(unit)
    Recover.consider(unit)
    BustBunker.consider(unit)
    ProtectTheWeak.consider(unit)
    FightOrFlight.consider(unit)
    Pursue.consider(unit)
    Pillage.consider(unit)
  }
}
