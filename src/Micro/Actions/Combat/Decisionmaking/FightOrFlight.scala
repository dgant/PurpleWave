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

    var decision: Option[Boolean] = None
    def decide(condition: () => Boolean, shouldEngage: Boolean) {
      if (decision.isEmpty && condition()) {
        decision = Some(shouldEngage)
      }
    }
  
    decide(() => unit.agent.canBerzerk,     true)
    decide(() => unit.effectivelyCloaked,   true)
    decide(() => Yolo.active,               true)
    decide(() =>  ! unit.agent.canFight,    false)
    decide(() => unit.underStorm,           false)
    decide(() => unit.underDisruptionWeb,   false)
    decide(() => unit.underDarkSwarm,       unit.unitClass.unaffectedByDarkSwarm)
    decide(() => unit.flying && unit.matchups.threats.forall(_.topSpeed < unit.topSpeed) && unit.matchups.framesOfSafetyDiffused > 24, true)
    if (decision.isDefined) {
      unit.agent.shouldEngage = decision.get
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
    val retreatReport = battle.estimationSimulationRetreat.reportCards.get(unit)
    
    if (attackReport.isEmpty)   return default
    if (retreatReport.isEmpty)  return default
  
    val attackGain    = attackReport.get.valueDealt
    val attackLoss    = attackReport.get.valueReceived
    val retreatGain   = retreatReport.get.valueDealt
    val retreatLoss   = retreatReport.get.valueReceived
    val output        = attackGain + retreatLoss / With.configuration.retreatPreference - retreatGain - attackLoss / battle.analysis.desireMultiplier
    output
  }
}
