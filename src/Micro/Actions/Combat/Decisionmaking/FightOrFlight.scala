package Micro.Actions.Combat.Decisionmaking

import Lifecycle.With
import Micro.Actions.Action
import Planning.Yolo
import ProxyBwapi.Races.Protoss
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
  
    decide(true,        () => unit.agent.canBerzerk)
    decide(true,        () => unit.effectivelyCloaked)
    decide(true,        () => Yolo.active)
    decide(true,        () => unit.matchups.threats.isEmpty)
    decide(false,       () => ! unit.agent.canFight)
    decide(false,       () => unit.underDisruptionWeb && ! unit.flying)
    decide(false,       () => unit.underDarkSwarm     && ! unit.unitClass.unaffectedByDarkSwarm && unit.matchups.targetsInRange.forall(t => ! t.flying || t.underDarkSwarm))
    decide(true,        () => unit.matchups.targets.exists(_.matchups.targetsInRange.exists(ally => ally.unitClass.isWorker && ally.base.exists(_.owner.isUs))))
    decide(true,        () => unit.matchups.allies.exists(ally =>
          ally.canAttack
      &&  ally.unitClass.topSpeed <= Protoss.HighTemplar.topSpeed
      &&  ally.subjectiveValue >= unit.subjectiveValue
      && (
           ally.matchups.targetsInRange.nonEmpty
        || ally.matchups.framesOfSafetyDiffused < unit.matchups.framesOfSafetyDiffused)))
    
    if (decision.isDefined) {
      unit.agent.shouldEngage = decision.get
      return
    }
    
    val estimationAttack  = unit.battle.map(_.estimationSimulationAttack)
    val estimationRetreat = unit.battle.map(_.estimationSimulationRetreat)
    unit.agent.netEngagementValue = unit.battle.map(_.netEngageValue).getOrElse(0.0)
    
    lazy val motivated = unit.agent.netEngagementValue > 0.0 && estimationAttack.exists(e => e.costToEnemy > e.costToUs * With.configuration.lastStandMinimumValueRatio)
    val output = unit.battle.isEmpty || motivated
    unit.agent.shouldEngage = output
  }
}
