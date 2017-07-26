package Micro.Actions.Combat.Decisionmaking

import Mathematics.PurpleMath
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
  
    val doomed = unit.matchups.doomed
    val matchups       = unit.matchups.inFrames(24)
    
    unit.action.desireTeam        = unit.battle.map(_.desire).getOrElse(1.0)
    unit.action.desireIndividual  = PurpleMath.nanToInfinity(matchups.vpfDealingDiffused / matchups.vpfReceivingDiffused)
    unit.action.desireTotal       = unit.action.desireTeam * unit.action.desireIndividual
  
    if (doomed) {
      Engage.consider(unit)
    }
    if (unit.action.desireTotal < 1.0) {
      Disengage.consider(unit)
    }
    Engage.consider(unit)
  }
}
