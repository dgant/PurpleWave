
package Micro.Actions.Combat.Decisionmaking

import Information.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Actions.Action
import Micro.Actions.Transportation.Caddy.Shuttling
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

object FightOrFlight extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove
  }

  def potentiallyUseful(unit: FriendlyUnitInfo): Boolean = {
    if (unit.matchups.targets.nonEmpty) return true
    if (unit.unitClass.spells.exists(spell => With.self.hasTech(spell) && unit.energy >= spell.energyCost)) return true
    if (unit.unitClass.isDetector && ! unit.matchups.enemies.exists(t => t.cloaked && ( ! unit.agent.canFocus || unit.squadenemies.contains(t)))) return true
    false
  }
  
  override def perform(unit: FriendlyUnitInfo) {
  
    var decision: Option[Boolean] = None
  
    def decide(shouldEngage: Boolean, description: String, condition: () => Boolean) {
      if (decision.isEmpty && condition()) {
        unit.agent.fightReason = description
        decision = Some(shouldEngage)
      }
    }

    decide(true,  "YOLO",         () => With.yolo.active() && unit.canAttack)
    decide(true,  "Bored",        () => unit.battle.isEmpty)
    decide(true,  "No threats",   () => unit.matchups.threats.isEmpty)
    decide(false, "Pacifist",     () => ! unit.agent.canFight)
    decide(true,  "CantFlee",     () => ! unit.agent.canFlee)
    decide(true,  "Berzerk",      () => With.frame < GameTime(6, 0)() && unit.isAny(Protoss.Zealot, Zerg.Zergling) && unit.base.exists(b => b.owner.isEnemy || b.isNaturalOf.exists(_.owner.isEnemy)) && unit.matchups.threats.exists(t => t.is(Terran.Vulture) && t.matchups.catchers.isEmpty))
    decide(true,  "Hug",          () => ! unit.flying && unit.matchups.targets.exists(t => unit.pixelDistanceEdge(t) < t.pixelRangeMin))
    decide(false, "Scarabs",      () => unit.is(Protoss.Reaver) && unit.scarabCount == 0)
    decide(true,  "Cloaked",      () => unit.effectivelyCloaked || (unit.is(Terran.Wraith) && unit.energy >= 50 && unit.matchups.enemyDetectors.isEmpty && With.self.hasTech(Terran.WraithCloak)))
    decide(true,  "Lurking",      () => unit.is(Zerg.Lurker) && unit.matchups.enemyDetectors.isEmpty)
    decide(false, "Useless",      () => unit.matchups.threats.nonEmpty && unit.loadedUnits.isEmpty && ! potentiallyUseful(unit))
    decide(false, "Drained",      () => ! unit.canAttack && unit.energyMax > 0 && unit.unitClass.spells.forall(s => s.energyCost > unit.energy || ! With.self.hasTech(s)))
    decide(true,  "Scourge",      () => unit.is(Zerg.Scourge) && unit.matchups.targets.exists(target => target.canAttack(unit) && target.matchups.targetsInRange.nonEmpty))
    decide(false, "Disrupted",    () => unit.underDisruptionWeb && ! unit.flying)
    decide(false, "Swarmed",      () => unit.underDarkSwarm && ! unit.unitClass.unaffectedByDarkSwarm && unit.matchups.targetsInRange.forall(t => ! t.flying || t.underDarkSwarm))
    decide(true,  "CanAbuse",     () => unit.matchups.threats.forall(t => unit.canAttack(t) && unit.topSpeed > t.topSpeed && unit.pixelRangeAgainst(t) > t.pixelRangeAgainst(unit) + 32))
    decide(false, "WaitForSiege", () => unit.is(Terran.SiegeTankUnsieged) && unit.matchups.threats.exists(_.is(Protoss.Dragoon)) && With.units.ours.exists(_.techProducing.contains(Terran.SiegeMode)) && unit.matchups.allies.exists(a => a.is(Terran.Bunker) && a.complete && ! With.blackboard.wantToAttack()))
    decide(true,  "Energized", () =>
      unit.matchups.allies.exists(ally =>
        ally.is(Protoss.ShieldBattery)
        && ally.complete
        && ally.energy > 20
        && ally.pixelDistanceEdge(unit, otherAt = ByOption.minBy(unit.matchups.targets.view.map(unit.pixelToFireAt))(unit.pixelDistanceCenter).getOrElse(unit.pixelCenter)) < 72)
    )

    decide(true, "Workers", () => unit.matchups.allies.exists(u => u.friendly.isDefined && {
      val ally = u.friendly.get
      val output = (
        ally.unitClass.isWorker
        && ally.visibleToOpponents
        && ally.agent.toGather.exists(_.pixelDistanceEdge(ally) < With.configuration.workerDefenseRadiusPixels)
        && ally.matchups.pixelsOutOfNonWorkerRange <= 16 + unit.matchups.pixelsOutOfNonWorkerRange
        && ally.matchups.threats.exists(threat => u.canAttack && threat.framesToGetInRange(ally) <= 8 + unit.framesToGetInRange(threat))
      )
      output
    }))

    decide(false, "Hazard", () => {
      With.blackboard.mcrs() && {
        val occupancy = With.coordinator.gridPathOccupancy.get(unit.tileIncludingCenter)
        val output: Boolean = (
          ! unit.flying
            && occupancy > 0
            && occupancy + unit.unitClass.sqrtArea > 24
            && unit.matchups.allies.exists(ally =>
              ! ally.flying
              && ! ally.friendly.exists(_.agent.shouldEngage)
              && ally.pixelDistanceEdge(unit) < 32))
        output
      }
    })

    val getaway = "Getaway"
    decide(true, "Anchors", () => unit.matchups.allies.view.map(_.friendly).filter(_.isDefined).map(_.get).exists(ally => (
      ! ally.unitClass.isWorker
      && ally.agent.fightReason != getaway
      && ! ally.loaded
      && (ally.canAttack || (ally.unitClass.rawCanAttack && ally.unitClass.isBuilding) || ally.is(Zerg.CreepColony))
      && (ally.unitClass.topSpeed <= Protoss.HighTemplar.topSpeed)
      && (ally.subjectiveValue * (if (ally.unitClass.isBuilding) 2.0 else 1.0) > unit.subjectiveValue)
      && (ally.matchups.framesOfSafety < 24 + Math.max(0, unit.matchups.framesOfSafety) || ( ! ally.unitClass.isBuilding && ally.matchups.targetsInRange.nonEmpty))
      && (ally.friendly.forall(_.agent.ride.exists(_.pixelDistanceEdge(ally) > 96)) || ally.matchups.framesOfSafety <= PurpleMath.clamp(unit.matchups.framesOfSafety, 0, 3))
      && (ally.unitClass.isSpellcaster || (ally.matchups.threats.exists(t => ! t.unitClass.isWorker && t.topSpeed > ally.topSpeed && unit.canAttack(t)) && (ally.agent.shouldEngage || ally.matchups.targetsInRange.nonEmpty)))
    )))

    decide(true, getaway, () => unit.agent.ride.exists(ride => {
      val rideDistance = Math.max(0.0, ride.pixelDistanceCenter(unit) - Shuttling.pickupRadius - 32)
      val rideWait = PurpleMath.nanToInfinity(rideDistance / (ride.topSpeed + unit.topSpeed))
      (unit.isAny(Protoss.Reaver, Protoss.HighTemplar)
        && ! unit.matchups.threats.exists(t => t.isSiegeTankSieged() && t.pixelsToGetInRange(unit) < 48)
        && (rideWait <= Math.max(0.0, unit.matchups.framesOfSafety)
          || (unit.loaded && unit.matchups.framesOfSafety > unit.cooldownMaxAirGround)))
    }))

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
    unit.agent.fightReason = "Sim"
    unit.agent.shouldEngage = shouldEngage

    if (With.blackboard.mcrs()) {
      unit.agent.shouldEngage = unit.mcrs.shouldFight
    }
  }
}
