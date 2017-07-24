package Micro.Actions.Combat.Decisionmaking

import Micro.Actions.Action
import Planning.Yolo
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object FightOrFlight extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.action.canFight
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    if (Yolo.active) {
      Engage.consider(unit)
    }
    else if (unit.matchups.threats.isEmpty) {
      Engage.consider(unit)
    }
    else if (unit.matchups.netValuePerFrameDiffused > 0.0) {
      Engage.consider(unit)
    }
    else if (unit.matchups.netValuePerFrameCurrently > 0.0) {
      Engage.consider(unit)
    }
    
    if (unit.matchups.threatsViolent.nonEmpty && unit.matchups.targets.isEmpty) {
      Disengage.consider(unit)
    }

    if (unit.battle.exists(_.shouldRetreat) &&
      (
        unit.matchups.netValuePerFrameCurrently < 0.0 ||
        unit.matchups.netValuePerFrameDiffused < 0.0
      )) {
      Disengage.consider(unit)
    }
    Engage.consider(unit)
  }
}
