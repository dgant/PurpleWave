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
  
    val framesAhead       = Math.min(24 * 3, unit.matchups.framesToLiveDiffused.toInt)
    val doomed            = unit.matchups.doomed
    val futureMatchups    = unit.matchups.ifAt(framesAhead)
    val costOfRetreating  = unit.matchups.framesToRetreatDiffused * unit.matchups.vpfReceivingDiffused //TODO: Kill this. Leaving this here while debugging
    
    unit.action.desireTeam        = unit.battle.map(_.desire).getOrElse(0.0)
    unit.action.desireIndividual  = unit.battle.flatMap(_.estimationSimulation.reportCards.get(unit).map(_.netValuePerFrame)).getOrElse(0.0)
    unit.action.desireTotal       = unit.action.desireTeam + unit.action.desireIndividual // Vanity metric, for now
  
    if (doomed) {
      Engage.consider(unit)
    }
    if (unit.action.desireTeam < 0.0 && unit.action.desireIndividual < 0.0) {
      Disengage.consider(unit)
    }
    Engage.consider(unit)
  }
}
