package Micro.Actions.Basic

import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Unstick extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && ! unit.flying
    && ! unit.loaded
    && ! unit.unitClass.floats
    && unit.canAttack
    && unit.seeminglyStuck
  )
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    Commander.stop(unit)
  }
}
