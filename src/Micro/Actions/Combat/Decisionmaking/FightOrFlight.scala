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
    decide(true,  "CantFlee",   () => ! unit.agent.canFlee)
    decide(false, "Scarabs",    () => unit.is(Protoss.Reaver) && unit.scarabCount == 0)
    decide(true,  "Cloaked",    () => unit.effectivelyCloaked)
    decide(true,  "Lurking",    () => unit.is(Zerg.Lurker) && unit.matchups.enemyDetectors.isEmpty)
    decide(false, "Pacifist",   () => ! unit.agent.canFight)
    decide(false, "Useless",    () => unit.canAttack && unit.energyMax == 0 && unit.matchups.targets.isEmpty && unit.matchups.threats.nonEmpty)
    decide(true,  "Scourge",    () => unit.is(Zerg.Scourge) && unit.matchups.targets.exists(target => target.canAttack(unit) && target.matchups.targetsInRange.nonEmpty))
    decide(false, "Disrupted",  () => unit.underDisruptionWeb && ! unit.flying)
    decide(false, "Swarmed",    () => unit.underDarkSwarm && !unit.unitClass.unaffectedByDarkSwarm && unit.matchups.targetsInRange.forall(t => !t.flying || t.underDarkSwarm))
    decide(true,  "Workers",    () => unit.matchups.allies.exists(u => {
      val ally = u.friendly.get
      val base = u.base.filter(_.owner.isUs)
      val output = (
        ally.unitClass.isWorker
        && base.isDefined
        && base.exists(ally.base.contains)
        && ally.visibleToOpponents
        && ally.matchups.framesOfSafety <= Math.max(0, unit.matchups.framesOfSafety)
        && ally.base.exists(_.units.exists(resource => resource.resourcesLeft > 0 && resource.pixelDistanceCenter(ally) < With.configuration.workerDefenseRadiusPixels))
      )
      output
    }))
    
    decide(true, "Anchors", () => unit.matchups.allies.exists(ally =>
      ! ally.unitClass.isWorker
      && (ally.canAttack || (ally.unitClass.rawCanAttack && ally.unitClass.isBuilding) || ally.is(Zerg.CreepColony))
      && ally.unitClass.topSpeed <= Protoss.HighTemplar.topSpeed
      && (ally.subjectiveValue > unit.subjectiveValue || ally.unitClass.isBuilding)
      && (ally.matchups.targetsInRange.nonEmpty || ( ! ally.canAttack && ally.matchups.enemies.exists(_.pixelDistanceEdge(ally) < ally.effectiveRangePixels)))
      && ally.matchups.framesOfSafety <= unit.matchups.framesOfSafety))
  
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
