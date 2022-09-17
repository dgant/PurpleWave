package Micro.Actions.Transportation

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Evacuate extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.isTransport && unit.loadedUnits.nonEmpty && unit.matchups.framesToLive < unit.loadedUnits.size * 24
  )
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    With.logger.micro(f"$unit evacuating passengers ${unit.loadedUnits.map(_.toString).mkString(", ")}")
    unit.loadedUnits.headOption.foreach(Commander.unload(unit, _))
    Retreat.delegate(unit)
  }
}
