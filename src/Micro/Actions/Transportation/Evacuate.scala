package Micro.Actions.Transportation

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Evacuate extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.isTransport && unit.loadedUnits.nonEmpty && unit.matchups.framesToLive < unit.loadedUnits.size * 24
  )
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    unit.loadedUnits.headOption.foreach(With.commander.unload(unit, _))
    Retreat.delegate(unit)
  }
}
