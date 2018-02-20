package Micro.Actions.Combat.Decisionmaking

import Lifecycle.With
import Micro.Actions.Action
import Planning.Yolo
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object FightOrFlight extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove
  }
  
  override def perform(unit: FriendlyUnitInfo) {

    var decision: Option[Boolean] = None
    def decide(shouldEngage: Boolean, description: String, condition: () => Boolean) {
      if (decision.isEmpty && condition()) {
        unit.agent.fightReason = description
        decision = Some(shouldEngage)
      }
    }
  
    decide(true,  "Berzerk",    () => unit.agent.canBerzerk)
    decide(true,  "YOLO",       () => Yolo.active)
    decide(true,  "Bored",      () => unit.battle.isEmpty)
    decide(true,  "No threats", () => unit.matchups.threats.isEmpty)
    decide(true,  "Cloaked",    () => unit.effectivelyCloaked)
    decide(true,  "Lurking",    () => unit.is(Zerg.Lurker) && unit.matchups.enemyDetectors.isEmpty)
    decide(false, "Pacifist",   () => ! unit.agent.canFight)
    decide(false, "Disrupted",  () => unit.underDisruptionWeb && ! unit.flying)
    decide(false, "Swarmed",    () => unit.underDarkSwarm     && ! unit.unitClass.unaffectedByDarkSwarm && unit.matchups.targetsInRange.forall(t => ! t.flying || t.underDarkSwarm))
    decide(true,  "Workers",    () => unit.matchups.targets.exists(_.matchups.targetsInRange.exists(ally => ally.unitClass.isWorker && ally.base.exists(_.owner.isUs))))
    decide(true,  "Anchors",    () => unit.matchups.allies.exists(ally =>
      ! ally.unitClass.isWorker
      &&  ally.canAttack
      &&  ally.unitClass.topSpeed <= Protoss.HighTemplar.topSpeed
      &&  ally.subjectiveValue >= unit.subjectiveValue
      && (
           ally.matchups.targetsInRange.nonEmpty
        || ally.matchups.framesOfSafetyDiffused < unit.matchups.framesOfSafetyDiffused)))
    
    if (decision.isDefined) {
      unit.agent.shouldEngage = decision.get
      return
    }
    
    lazy val estimationAttack     = unit.battle.map(_.estimationSimulationAttack)
    lazy val estimationRetreat    = unit.battle.map(_.estimationSimulationRetreat)
    lazy val reportAttack         = estimationAttack.map(_.reportCards.get(unit)).getOrElse(None)
    lazy val reportRetreat        = estimationRetreat.map(_.reportCards.get(unit)).getOrElse(None)
    lazy val netAttackValue       = estimationAttack.map(_.netValue).getOrElse(0.0)
    lazy val netEngagementValue   = unit.battle.map(_.netEngageValue).getOrElse(0.0)
    lazy val attackingProfitable  = netAttackValue >= 0.0
    lazy val attackingIsBetter    = netEngagementValue >= 0.0
    lazy val attackingHasPurpose  = estimationAttack.exists(e => e.costToEnemy > e.costToUs * With.configuration.lastStandMinimumValueRatio)
    lazy val deadAttacking        = reportAttack.exists(_.dead)
    lazy val deadRetreating       = reportRetreat.exists(_.dead)
    lazy val shouldEngage         = attackingIsBetter && (attackingProfitable || attackingHasPurpose)
    if (shouldEngage) {
      if (attackingProfitable) {
        unit.agent.fightReason = "Profiting"
      }
      else {
        unit.agent.fightReason = "Resigned"
      }
    }
    else {
      if (deadAttacking && deadRetreating) {
        unit.agent.fightReason = "Terrified"
      }
      else if (deadAttacking) {
        unit.agent.fightReason = "Relieved"
      }
      else if (deadRetreating) {
        unit.agent.fightReason = "Abandoned"
      }
      else {
        unit.agent.fightReason = "Hungry"
      }
    }
  
    unit.agent.netEngagementValue = netEngagementValue
    unit.agent.shouldEngage = shouldEngage
  }
}
