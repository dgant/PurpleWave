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
    def decide(shouldEngage: Boolean, condition: () => Boolean) {
      if (decision.isEmpty && condition()) {
        decision = Some(shouldEngage)
      }
    }
  
    lazy val ignoreWeb    = unit.flying
    lazy val ignoreSwarm  = unit.unitClass.unaffectedByDarkSwarm
    decide(true,        () => unit.agent.canBerzerk)
    decide(true,        () => unit.effectivelyCloaked)
    decide(true,        () => Yolo.active)
    decide(false,       () => ! unit.agent.canFight)
    decide(ignoreWeb,   () => unit.underDisruptionWeb)
    decide(ignoreSwarm, () => unit.underDarkSwarm)
    decide(true,        () => unit.matchups.allies.exists(ally => ally.unitClass.isStaticDefense && ally.matchups.targetsInRange.nonEmpty))
    decide(true,        () => unit.base.exists(_.owner.isUs) && unit.matchups.targets.exists(target => target.matchups.targetsInRange.exists(_.unitClass.isWorker)))
    decide(true,        () => unit.flying && unit.matchups.threats.forall(_.topSpeed < unit.topSpeed) && unit.matchups.framesOfSafetyDiffused > 0.0)
    if (decision.isDefined) {
      unit.agent.shouldEngage = decision.get
      return
    }
    
    unit.agent.desireTeam        = unit.battle.map(_.netEngageValue).getOrElse(0.0)
    unit.agent.desireIndividual  = individualDesire(unit)
    
    lazy val motivatedByDoom       = unit.matchups.doomedDiffused && unit.battle.exists(_.estimationSimulationRetreat.reportCards.get(unit).exists(_.dead))
    lazy val motivatedIndividually = unit.agent.desireIndividual > 0.0
    lazy val motivatedCollectively = unit.agent.desireTeam       > 0.0
    unit.agent.shouldEngage = unit.battle.isEmpty || motivatedByDoom || motivatedIndividually || motivatedCollectively
  }
  
  private def individualDesire(unit: FriendlyUnitInfo): Double = {
    
    val default = Double.NegativeInfinity
    if (unit.battle.isEmpty) return default
    
    val battle        = unit.battle.get
    val attackReport  = battle.estimationSimulationAttack.reportCards.get(unit)
    val retreatReport = battle.estimationSimulationRetreat.reportCards.get(unit)
    
    if (attackReport.isEmpty)         return default
    if (retreatReport.isEmpty)        return default
    if (attackReport.get.killed <= 0) return default // Don't fight just to deal meaningless damage
  
    val attackGain    = attackReport.get.valueDealt
    val attackLoss    = attackReport.get.valueReceived
    val retreatGain   = retreatReport.get.valueDealt
    val retreatLoss   = retreatReport.get.valueReceived
    val output        = With.blackboard.aggressionRatio * attackGain + retreatLoss - retreatGain - attackLoss
    output
  }
}
