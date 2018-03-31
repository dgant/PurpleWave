package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Unstick extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && ! unit.flying
    && unit.canAttack
    && unit.seeminglyStuck
  )
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    With.commander.stop(unit)
  }
}
