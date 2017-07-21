package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Unstick extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMoveThisFrame &&
    unit.seeminglyStuck   &&
    ! unit.is(Protoss.Carrier)
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    With.commander.stop(unit)
  }
}
