
package Micro.Actions.Combat.Decisionmaking

import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Actions.Action
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.{ByOption, Minutes}

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

    decide(true,  "Static",       () => ! unit.canMove)
    decide(true,  "YOLO",         () => With.yolo.active() && unit.canAttack)
    decide(true,  "Bored",        () => unit.battle.isEmpty)
    decide(true,  "No threats",   () => unit.matchups.threats.isEmpty)
    decide(true,  "Irradiated",   () => unit.irradiated && unit.unitClass.isOrganic)
    decide(true,  "CantFlee",     () => ! unit.agent.canFlee)
    decide(true,  "Berzerk",      () => With.frame < Minutes(6)() && unit.isAny(Protoss.Zealot, Zerg.Zergling) && unit.base.exists(b => b.owner.isEnemy || b.isNaturalOf.exists(_.owner.isEnemy)) && unit.matchups.threats.exists(t => t.is(Terran.Vulture) && t.matchups.catchers.isEmpty))
    decide(true,  "Hug",          () => ! unit.flying && unit.matchups.targets.exists(t => unit.pixelDistanceEdge(t) < t.pixelRangeMin))
    decide(true,  "Cloaked",      () => unit.effectivelyCloaked || (unit.is(Terran.Wraith) && unit.energy >= 50 && unit.matchups.enemyDetectors.isEmpty && With.self.hasTech(Terran.WraithCloak)))
    decide(true,  "Lurking",      () => unit.is(Zerg.Lurker) && unit.matchups.enemyDetectors.isEmpty)
    decide(true,  "Detonated",    () => unit.isAny(Zerg.InfestedTerran, Zerg.Scourge) && unit.matchups.targets.exists(t => t.canAttack(unit) && t.matchups.targetsInRange.nonEmpty))
    decide(false, "CantFight",    () => ! unit.agent.canFight)
    decide(false, "Scarabs",      () => unit.is(Protoss.Reaver) && unit.scarabs == 0 && ! unit.trainee.exists(_.remainingCompletionFrames < Math.max(unit.matchups.framesOfSafety, unit.cooldownLeft)))
    decide(false, "Drained",      () => ! unit.canAttack && unit.energyMax > 0 && unit.unitClass.spells.forall(s => s.energyCost > unit.energy || ! With.self.hasTech(s)))
    decide(false, "Disrupted",    () => unit.underDisruptionWeb && ! unit.flying && unit.matchups.threats.exists(t => t.flying || ! t.underDisruptionWeb))
    decide(false, "Swarmed",      () => unit.underDarkSwarm && ! unit.unitClass.unaffectedByDarkSwarm && unit.matchups.targetsInRange.forall(t => ! t.flying || t.underDarkSwarm))
    decide(false, "BidingSiege",  () => unit.is(Terran.SiegeTankUnsieged) && unit.matchups.threats.exists(_.is(Protoss.Dragoon)) && With.units.ours.exists(_.techProducing.contains(Terran.SiegeMode)) && unit.matchups.allies.exists(a => a.is(Terran.Bunker) && a.complete && ! With.blackboard.wantToAttack()))
    decide(true,  "Energized", () =>
      unit.unitClass.maxShields > 10
      && unit.matchups.allies.exists(ally =>
        ally.is(Protoss.ShieldBattery)
        && ally.complete
        && ally.energy > 20
        && ally.pixelDistanceEdge(unit, otherAt = ByOption.minBy(unit.matchups.targets.view.map(unit.pixelToFireAt))(unit.pixelDistanceCenter).getOrElse(unit.pixel)) < 72))

    decide(true, "Anchors", () => unit.matchups.allies.view
      .map(_.friendly)
      .filter(_.isDefined)
      .map(_.get)
      .exists(ally => (
        ! ally.unitClass.isWorker
        && ! ally.loaded
        && ally.visibleToOpponents
        && (ally.canAttack || (ally.unitClass.rawCanAttack && ally.unitClass.isBuilding) || ally.is(Zerg.CreepColony))
        && (ally.unitClass.topSpeed <= Protoss.HighTemplar.topSpeed)
        && (ally.subjectiveValue * (if (ally.unitClass.isBuilding) 2.0 else 1.0) > unit.subjectiveValue)
        && (ally.matchups.framesOfSafety < 24 + Math.max(0, unit.matchups.framesOfSafety) || ( ! ally.unitClass.isBuilding && ally.matchups.targetsInRange.nonEmpty))
        && (ally.friendly.forall(_.agent.ride.exists(_.pixelDistanceEdge(ally) > 96)) || ally.matchups.framesOfSafety <= PurpleMath.clamp(unit.matchups.framesOfSafety, 0, 3))
        && (ally.unitClass.isSpellcaster || (ally.matchups.threats.exists(t => ! t.unitClass.isWorker && t.topSpeed > ally.topSpeed && unit.canAttack(t)) && (ally.agent.shouldEngage || ally.matchups.targetsInRange.nonEmpty)))
      )))

    if (decision.isDefined) {
      unit.agent.shouldEngage = decision.get
      unit.agent.fightHysteresisFrames = 0
      return
    }

    applyEstimation(unit)
  }
  
  private def applyEstimation(unit: FriendlyUnitInfo) {
    if (unit.battle.isEmpty) return
    val battle = unit.battle.get
    var shouldEngage = false
    unit.agent.fightReason = "No"
    if (battle.judgement.get.shouldFight) {
      shouldEngage = true
      unit.agent.fightReason = "Yes"
    }
  
    if (unit.agent.shouldEngage != shouldEngage) {
      unit.agent.fightHysteresisFrames = With.configuration.battleHysteresisFrames
    }
    unit.agent.shouldEngage = shouldEngage

    if (With.blackboard.mcrs()) {
      unit.agent.shouldEngage = unit.mcrs.shouldFight
    }
  }
}
