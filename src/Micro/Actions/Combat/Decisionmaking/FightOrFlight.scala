package Micro.Actions.Combat.Decisionmaking

import Micro.Actions.Action
import Planning.Yolo
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object FightOrFlight extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.canFight
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    if (Yolo.active) {
      Engage.consider(unit)
    }
  
    unit.agent.desireTeam        = unit.battle.map(_.desire).getOrElse(0.0)
    unit.agent.desireIndividual  = unit.battle.flatMap(_.estimationSimulation.reportCards.get(unit).map(_.netValuePerFrame)).getOrElse(0.0)
    unit.agent.desireTotal       = unit.agent.desireTeam + unit.agent.desireIndividual // Vanity metric, for now
  
    if (unit.matchups.doomed) {
      Engage.consider(unit)
    }
    if (unit.agent.desireTeam < 0.0 && unit.agent.desireIndividual < 0.0) {
      Disengage.consider(unit)
    }
    Engage.consider(unit)
  }
}
