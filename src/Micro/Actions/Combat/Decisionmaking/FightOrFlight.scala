
package Micro.Actions.Combat.Decisionmaking

import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Actions.Action
import Micro.Actions.Transportation.Caddy.Shuttling
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
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

    decide(true,  "YOLO",         () => With.yolo.active())
    decide(true,  "Bored",        () => unit.battle.isEmpty)
    decide(true,  "No threats",   () => unit.matchups.threats.isEmpty)
    decide(false, "Pacifist",     () => ! unit.agent.canFight)
    decide(true,  "CantFlee",     () => ! unit.agent.canFlee)
    decide(true,  "Hug",          () => ! unit.flying && unit.matchups.targets.exists(t => unit.pixelDistanceEdge(t) < t.pixelRangeMin))
    decide(false, "Scarabs",      () => unit.is(Protoss.Reaver) && unit.scarabCount == 0)
    decide(true,  "Cloaked",      () => unit.effectivelyCloaked)
    decide(true,  "Lurking",      () => unit.is(Zerg.Lurker) && unit.matchups.enemyDetectors.isEmpty)
    decide(false, "Useless",      () => unit.energyMax == 0 && unit.matchups.threats.nonEmpty && unit.loadedUnits.isEmpty && (
      (unit.canAttack && ! unit.matchups.targets.exists(t => ! unit.agent.canFocus || unit.squadenemies.contains(t)))
      || (unit.unitClass.isDetector && ! unit.matchups.enemies.exists(t => t.cloaked && ( ! unit.agent.canFocus || unit.squadenemies.contains(t))))))
    decide(false, "Drained",      () => ! unit.canAttack && unit.energyMax > 0 && ! unit.unitClass.spells.forall(s => s.energyCost > unit.energy || ! With.self.hasTech(s)))
    decide(true,  "Scourge",      () => unit.is(Zerg.Scourge) && unit.matchups.targets.exists(target => target.canAttack(unit) && target.matchups.targetsInRange.nonEmpty))
    decide(false, "Disrupted",    () => unit.underDisruptionWeb && ! unit.flying)
    decide(false, "Swarmed",      () => unit.underDarkSwarm && ! unit.unitClass.unaffectedByDarkSwarm && unit.matchups.targetsInRange.forall(t => ! t.flying || t.underDarkSwarm))
    decide(false, "WaitForSiege", () => unit.is(Terran.SiegeTankUnsieged) && unit.matchups.threats.exists(_.is(Protoss.Dragoon)) && With.units.ours.exists(_.techProducing.contains(Terran.SiegeMode)) && unit.matchups.allies.exists(a => a.is(Terran.Bunker) && a.complete))
    decide(true,  "Commit", () =>
      unit.visibleToOpponents
      && unit.unitClass.melee
      && unit.base.exists(base => base.owner.isEnemy || base.isNaturalOf.exists(_.owner.isEnemy))
      && unit.matchups.threats.exists(threat =>
        unit.canAttack(threat)
        && threat.topSpeed > unit.topSpeed
        && threat.pixelRangeAgainst(unit) > unit.pixelRangeAgainst(threat)
        && threat.matchups.threats.forall(ally => ally.topSpeed < threat.topSpeed && ally.pixelRangeAgainst(threat) < threat.pixelRangeAgainst(ally))))

    //decide(true,  "Hodor",      () => unit.matchups.alliesInclSelf.forall(_.base.exists(_.isOurMain)) && unit.matchups.threats.exists(t => ! t.flying && t.base.exists(_.isOurMain)) && unit.matchups.threats.exists(t => ! t.flying && ! t.base.exists(_.isOurMain)))

    decide(true, "Workers", () => unit.matchups.allies.exists(u => u.friendly.isDefined && {
      val ally = u.friendly.get
      val base = u.base.filter(_.owner.isUs)
      val output = (
        ally.unitClass.isWorker
        && base.isDefined
        && base.exists(ally.base.contains)
        && ally.visibleToOpponents
        && ally.matchups.framesOfSafety <= Math.max(0, unit.matchups.framesOfSafety)
        && ally.base.exists(_.units.exists(resource =>
          resource.resourcesLeft > 0
          && resource.pixelDistanceCenter(ally) < With.configuration.workerDefenseRadiusPixels))
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
      && ally.unitClass.topSpeed <= Protoss.HighTemplar.topSpeed
      && (ally.subjectiveValue > unit.subjectiveValue || ally.unitClass.isBuilding)
      && ( ! ally.unitClass.isBuilding || ally.matchups.threatsInRange.nonEmpty)
      && ally.agent.ride.exists(_.pixelDistanceEdge(ally) > 96)) || ally.matchups.threatsInRange.nonEmpty
      && ally.matchups.framesOfSafety <= 12 + Math.max(0, unit.matchups.framesOfSafety)
    ))

    decide(true, getaway, () => unit.agent.ride.exists(ride => {
      val rideDistance = Math.max(0.0, ride.pixelDistanceCenter(unit) - Shuttling.pickupRadius - 32)
      val rideWait = PurpleMath.nanToInfinity(rideDistance / (ride.topSpeed + unit.topSpeed))
      rideWait <= Math.max(0.0, unit.matchups.framesOfSafety) || (unit.loaded && unit.matchups.framesOfSafety > unit.cooldownMaxAirGround)
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
