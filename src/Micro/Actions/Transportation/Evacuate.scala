package Micro.Actions.Transportation

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Techniques.Avoid
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Evacuate extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.isTransport && unit.matchups.framesToLive < unit.loadedUnits.size * 24
  )
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    With.commander.unload(unit, unit.loadedUnits.head)
    Avoid.delegate(unit)
  }
}
