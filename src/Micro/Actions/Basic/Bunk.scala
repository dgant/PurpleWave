package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Bunk extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.toBoard.exists(_.is(Terran.Bunker))
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    if (unit.loaded) {
      if (unit.transport == unit.agent.toBoard) {
        With.commander.doNothing(unit)
      } else {
        unit.transport.foreach(With.commander.unload(_, unit))
      }
    } else {
      With.commander.rightClick(unit, unit.agent.toBoard.get)
    }
  }
}
