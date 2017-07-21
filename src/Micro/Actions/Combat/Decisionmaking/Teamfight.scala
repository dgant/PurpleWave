package Micro.Actions.Combat.Decisionmaking

import Micro.Actions.Action
import Planning.Yolo
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Teamfight extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.action.canFight
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    if (unit.matchups.threats.isEmpty) {
      Engage.consider(unit)
    }
    else if (unit.battle.exists(_.estimationGeometricOffense.weGainValue) || Yolo.active) {
      Engage.consider(unit)
    }
    else if (unit.battle.exists(_.estimationGeometricOffense.weLoseValue)) {
      Disengage.consider(unit)
    }
    Engage.consider(unit)
  }
}
