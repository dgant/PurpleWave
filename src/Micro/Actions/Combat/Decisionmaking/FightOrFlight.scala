package Micro.Actions.Combat.Decisionmaking

import Lifecycle.With
import Micro.Actions.Action
import Planning.Yolo
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object FightOrFlight extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove
  }
  
  override def perform(unit: FriendlyUnitInfo) {
  
    if (unit.effectivelyCloaked)            { unit.agent.shouldEngage = true;                                   return }
    if (Yolo.active)                        { unit.agent.shouldEngage = true;                                   return }
    if ( ! unit.agent.canFight)             { unit.agent.shouldEngage = false;                                  return }
    if (unit.underStorm)                    { unit.agent.shouldEngage = false;                                  return }
    if (unit.underDisruptionWeb)            { unit.agent.shouldEngage = false;                                  return }
    if (unit.underDarkSwarm)                { unit.agent.shouldEngage = unit.unitClass.unaffectedByDarkSwarm;   return }
    if (unit.flying
      && unit.matchups.threats.forall(_.topSpeed < unit.topSpeed)
      && unit.matchups.ifAt(24).threatsInRange.isEmpty) {
      unit.agent.shouldEngage = true
      return
    }
    
    unit.agent.desireTeam        = unit.battle.map(_.desire).getOrElse(0.0)
    unit.agent.desireIndividual  = individualDesire(unit)
    
    // Hysteresis
    val individualCaution           = 0.2
    val individualHysteresis        = 0.2
    val individualThreshold         = individualCaution + (if (unit.agent.shouldEngage) -individualHysteresis else individualHysteresis)
    val motivatedByDoom             = unit.matchups.doomedDiffused && unit.battle.exists(_.estimationSimulationRetreat.reportCards.get(unit).exists(_.dead))
    val motivatedIndividually       = unit.agent.desireIndividual > individualThreshold
    val motivatedCollectively       = unit.agent.desireTeam       > 0.0
    unit.agent.shouldEngage         = motivatedByDoom || motivatedIndividually || motivatedCollectively
  }
  
  private def individualDesire(unit: FriendlyUnitInfo): Double = {
    
    val default = Double.NegativeInfinity
    if (unit.battle.isEmpty) return default
    
    val battle        = unit.battle.get
    val attackReport  = battle.estimationSimulationAttack.reportCards.get(unit)
    val retreatReport = battle.estimationSimulationAttack.reportCards.get(unit)
    
    if (attackReport.isEmpty)   return default
    if (retreatReport.isEmpty)  return default
  
    val bonusDesire   = With.blackboard.aggressionRatio
    val attackGain    = attackReport.get.valueDealt
    val attackLoss    = attackReport.get.valueReceived
    val retreatGain   = attackReport.get.valueDealt
    val retreatLoss   = attackReport.get.valueReceived
    val output        = bonusDesire * attackGain + retreatLoss - attackLoss - retreatGain
    output
  }
}
