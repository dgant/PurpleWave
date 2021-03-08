package Micro.Actions.Basic

import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Bunk extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.toBoard.exists(Terran.Bunker)
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    if (unit.loaded) {
      if (unit.transport == unit.agent.toBoard) {
        Commander.doNothing(unit)
      } else {
        unit.transport.foreach(Commander.unload(_, unit))
      }
    } else {
      Commander.rightClick(unit, unit.agent.toBoard.get)
    }
  }
}
