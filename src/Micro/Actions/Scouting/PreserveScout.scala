package Micro.Actions.Scouting

import Micro.Actions.Action
import Micro.Actions.Combat.Decisionmaking.Leave
import Micro.Actions.Combat.Maneuvering.GooseChase
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object PreserveScout extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    (unit.matchups.framesOfSafety <= 12
      && unit.matchups.threats.exists( ! _.unitClass.isWorker))
      || unit.totalHealth < 6
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    GooseChase.consider(unit)
    Leave.delegate(unit)
  }
}
