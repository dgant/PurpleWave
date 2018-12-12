
package Micro.Actions.Combat.Decisionmaking

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Actions.Action
import Micro.Actions.Transportation.Caddy.Shuttling
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

    decide(true,  "YOLO",       () => Yolo.active)
    decide(true,  "Bored",      () => unit.battle.isEmpty)
    decide(true,  "No threats", () => unit.matchups.threats.isEmpty)
    decide(true,  "CantFlee",   () => ! unit.agent.canFlee)
    decide(false, "Scarabs",    () => unit.is(Protoss.Reaver) && unit.scarabCount == 0)
    decide(true,  "Cloaked",    () => unit.effectivelyCloaked)
    decide(true,  "Lurking",    () => unit.is(Zerg.Lurker) && unit.matchups.enemyDetectors.isEmpty)
    decide(true,  "Focused",    () => unit.agent.canFocus && (! unit.visibleToOpponents || unit.matchups.framesOfSafety > GameTime(0, 2)()) && With.framesSince(unit.lastFrameTakingDamage) > GameTime(0, 10)())
    decide(false, "Pacifist",   () => ! unit.agent.canFight)
    decide(false, "Useless",    () => unit.canAttack && unit.energyMax == 0 && unit.matchups.targets.isEmpty && unit.matchups.threats.nonEmpty)
    decide(false, "Drained",    () => ! unit.canAttack && unit.energyMax > 0 && ! unit.unitClass.spells.forall(s => s.energyCost > unit.energy || ! With.self.hasTech(s)))
    decide(true,  "Scourge",    () => unit.is(Zerg.Scourge) && unit.matchups.targets.exists(target => target.canAttack(unit) && target.matchups.targetsInRange.nonEmpty))
    decide(false, "Disrupted",  () => unit.underDisruptionWeb && ! unit.flying)
    decide(false, "Swarmed",    () => unit.underDarkSwarm && ! unit.unitClass.unaffectedByDarkSwarm && unit.matchups.targetsInRange.forall(t => ! t.flying || t.underDarkSwarm))
    decide(true,  "Hodor",      () => unit.matchups.alliesInclSelf.forall(_.base.exists(_.isOurMain)) && unit.matchups.threats.exists(_.base.exists(_.isOurMain)) && unit.matchups.threats.exists(!_.base.exists(_.isOurMain)))

    decide(true, "Workers", () => unit.matchups.allies.exists(u => u.friendly.isDefined && {
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

    decide(false, "Hazard", () => {
      With.configuration.enableMCRS && {
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

    decide(true, "Anchors", () => unit.matchups.allies.exists(ally =>
      ! ally.unitClass.isWorker
      && (ally.canAttack || (ally.unitClass.rawCanAttack && ally.unitClass.isBuilding) || ally.is(Zerg.CreepColony))
      && ally.unitClass.topSpeed <= Protoss.HighTemplar.topSpeed
      && (ally.subjectiveValue > unit.subjectiveValue || ally.unitClass.isBuilding)
      && ally.matchups.framesOfSafety <= 12 + Math.max(0, unit.matchups.framesOfSafety)))

    decide(true, "Escape", () => unit.agent.ride.exists(ride => {
      val rideDistance = Math.max(0.0, ride.pixelDistanceCenter(unit) - Shuttling.pickupRadius)
      val rideWait = PurpleMath.nanToInfinity(rideDistance / ride.topSpeed)
      rideWait <= Math.max(0.0, unit.matchups.framesOfSafety)
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
    unit.agent.fightReason = ""
    unit.agent.netEngagementValue = 0.0 // TODO
    unit.agent.shouldEngage = shouldEngage

    if (With.configuration.enableMCRS) {
      unit.agent.shouldEngage = unit.mcrs.shouldFight
    }
  }
}
