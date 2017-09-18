package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Unstick extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove &&
    unit.seeminglyStuck   &&
    ! unit.is(Protoss.Carrier) //Telling a Carrier to stop withdraws its interceptors. Really harmful. Never seen one get stuck anyhow.
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    With.commander.stop(unit)
  }
}
