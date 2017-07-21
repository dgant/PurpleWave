package Micro.Actions.Combat

import Micro.Actions.Action
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Cower extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    (
      unit.action.canCower      ||
      unit.is(Protoss.Observer) || //Dirty hacks -- need to do better than this
      unit.is(Protoss.Arbiter)
    ) &&
      unit.canMoveThisFrame           &&
      unit.matchups.threats.nonEmpty  &&
      unit.matchups.threats.exists(threat =>
        threat.topSpeed > unit.topSpeed ||
        threat.framesBeforeAttacking(unit) < 24.0)
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    Disengage.delegate(unit)
  }
}
