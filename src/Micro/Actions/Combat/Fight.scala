package Micro.Actions.Combat

import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Fight extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMoveThisFrame || unit.readyForAttackOrder
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    Cower.consider(unit)
    Sneak.consider(unit)
    BustBunker.consider(unit)
    ProtectTheWeak.consider(unit)
    Teamfight.consider(unit)
    Pursue.consider(unit)
    Pillage.consider(unit)
  }
}
