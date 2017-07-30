package Micro.Actions.Combat.Decisionmaking

import Micro.Actions.Action
import Planning.Yolo
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object FightOrFlight extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    true
  }
  
  override def perform(unit: FriendlyUnitInfo) {
  
    unit.agent.desireTeam        = 0.0
    unit.agent.desireIndividual  = 0.0
    unit.agent.desireTotal       = 0.0
    
    // Performance shortcuts
    if ( ! unit.agent.canFight) {
      unit.agent.shouldEngage = false
      return
    }
    
    if ( ! unit.canMove || Yolo.active) {
      unit.agent.shouldEngage = true
      return
    }
    
    unit.agent.desireTeam        = unit.battle.map(_.desire).getOrElse(0.0)
    unit.agent.desireIndividual  = unit.battle.flatMap(_.estimationSimulation.reportCards.get(unit).map(_.netValuePerFrame)).getOrElse(0.0)
    unit.agent.desireTotal       = unit.agent.desireTeam + unit.agent.desireIndividual // Vanity metric, for now
    
    // Hysteresis
    val hysteresis = 0.3
    val desireRequiredToEngage = if (unit.agent.shouldEngage) - hysteresis else hysteresis
    
    unit.agent.shouldEngage =
      unit.agent.canFight && (
        unit.matchups.doomed                                  ||
        unit.agent.desireIndividual >= desireRequiredToEngage ||
        unit.agent.desireTeam       >= desireRequiredToEngage)
  }
}
