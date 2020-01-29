package Micro.Actions.Scouting

import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.GooseChase
import Micro.Actions.Combat.Techniques.Avoid
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object PreserveScout extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    (unit.matchups.framesOfSafety <= 12
      && unit.matchups.threats.exists( ! _.unitClass.isWorker))
      || unit.totalHealth < 6
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    // FindBuildings can usually safely path around threats
    FindBuildingsWhenBored.delegate(unit)
    GooseChase.consider(unit)
    Avoid.delegate(unit)
  }
}
