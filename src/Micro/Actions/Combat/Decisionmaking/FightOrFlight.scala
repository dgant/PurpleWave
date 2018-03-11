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
  
    decide(true, "Berzerk", () => unit.agent.canBerzerk)
    decide(true, "CantFlee", () => ! unit.agent.canFlee)
    decide(true, "YOLO", () => Yolo.active)
    decide(true, "Bored", () => unit.battle.isEmpty)
    decide(true, "No threats", () => unit.matchups.threats.isEmpty)
    decide(true, "Cloaked", () => unit.effectivelyCloaked)
    decide(true, "Lurking", () => unit.is(Zerg.Lurker) && unit.matchups.enemyDetectors.isEmpty)
    decide(false, "Pacifist", () => !unit.agent.canFight)
    decide(false, "Disrupted", () => unit.underDisruptionWeb && !unit.flying)
    decide(false, "Swarmed", () => unit.underDarkSwarm && !unit.unitClass.unaffectedByDarkSwarm && unit.matchups.targetsInRange.forall(t => !t.flying || t.underDarkSwarm))
    decide(true, "Workers", () => unit.matchups.targets.exists(_.matchups.targetsInRange.exists(ally => ally.gathering || ally.constructing)))
    decide(true, "Anchors", () => unit.matchups.allies.exists(ally =>
      ! ally.unitClass.isWorker
        && ally.canAttack
        && ally.unitClass.topSpeed <= Protoss.HighTemplar.topSpeed
        && ally.subjectiveValue > unit.subjectiveValue
        && (
        ally.matchups.targetsInRange.nonEmpty
          || ally.matchups.framesOfSafetyDiffused < unit.matchups.framesOfSafetyDiffused)))
  
    if (decision.isDefined) {
      unit.agent.shouldEngage = decision.get
      unit.agent.combatHysteresisFrames = 0
      return
    }
    
    applyEstimation(unit)
  }
  
  private def applyEstimation(unit: FriendlyUnitInfo) {
    if (unit.battle.isEmpty) return
    val battle = unit.battle.get
    val shouldEngage = battle.shouldFight
  
    if (unit.agent.shouldEngage != shouldEngage) {
      unit.agent.combatHysteresisFrames = With.configuration.battleHysteresisFrames
    }
    unit.agent.fightReason = ""
    unit.agent.netEngagementValue = 0.0 // TODO
    unit.agent.shouldEngage = shouldEngage
  }
}
