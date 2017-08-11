package Micro.Actions.Combat.Decisionmaking

import Micro.Actions.Action
import Planning.Yolo
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object FightOrFlight extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove
  }
  
  override def perform(unit: FriendlyUnitInfo) {
  
    unit.agent.desireTeam        = 0.0
    unit.agent.desireIndividual  = 0.0
    unit.agent.desireTotal       = 0.0
    
    if ( ! unit.agent.canFight)             { unit.agent.shouldEngage = false;                                  return }
    if (unit.effectivelyCloaked)            { unit.agent.shouldEngage = true;                                   return }
    if (unit.underStorm)                    { unit.agent.shouldEngage = false;                                  return }
    if (unit.underDisruptionWeb)            { unit.agent.shouldEngage = false;                                  return }
    if (unit.underDarkSwarm)                { unit.agent.shouldEngage = unit.unitClass.unaffectedByDarkSwarm;   return }
    if ( ! unit.canMove || Yolo.active)     { unit.agent.shouldEngage = true;                                   return }
    
    unit.agent.desireTeam        = unit.battle.map(_.desire).getOrElse(0.0)
    unit.agent.desireIndividual  = unit.battle.flatMap(_.estimationSimulationAttack.reportCards.get(unit).map(_.netValuePerFrame)).getOrElse(0.0)
    unit.agent.desireTotal       = unit.agent.desireTeam + unit.agent.desireIndividual // Vanity metric, for now
    
    // Hysteresis
    val caution = 0.15
    val hysteresis = 0.0
    val desireRequiredToEngage = caution + (if (unit.agent.shouldEngage) 0.0 else hysteresis)
    
    unit.agent.shouldEngage =
      unit.agent.canFight && (
        unit.matchups.doomedDiffused                          ||
        unit.agent.desireIndividual >= desireRequiredToEngage ||
        unit.agent.desireTeam       >= desireRequiredToEngage)
  }
}
