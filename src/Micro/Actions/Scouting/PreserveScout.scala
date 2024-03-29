package Micro.Actions.Scouting

import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object PreserveScout extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    (unit.matchups.framesOfSafety <= 24
      && unit.matchups.threats.exists( ! _.unitClass.isWorker))
      || unit.totalHealth < 6
  )
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    // FindBuildings can usually safely path around threats
    SearchWhenBored.delegate(unit)
    Retreat.delegate(unit)
  }
}
