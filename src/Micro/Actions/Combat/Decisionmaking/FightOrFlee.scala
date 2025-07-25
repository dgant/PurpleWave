
package Micro.Actions.Combat.Decisionmaking

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.?
import Utilities.Time.Minutes
import Utilities.UnitFilters.{IsBurrowedLurker, IsSlowling, IsTank, IsWarrior, IsWorker}

object FightOrFlee extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.canMove

  //noinspection NameBooleanParameters
  override def perform(u: FriendlyUnitInfo): Unit = {

    var decision: Option[Boolean] = None

    def decide(shouldEngage: Boolean, description: String, condition: () => Boolean): Unit = {
      if (decision.isEmpty && condition()) {
        decision = Some(shouldEngage)
        u.agent.fightReason = description
      }
    }

    val berzerkCutoff = Minutes(6)()

    decide(true,  "Static",     () => ! u.canMove)
    decide(true,  "CantFlee",   () => ! u.intent.canFlee)
    decide(true,  "Committed",  () => u.agent.commit)
    decide(true,  "YOLO",       () => With.yolo.active && IsWarrior(u))
    decide(true,  "Irradiated", () => u.painfullyIrradiated)
    decide(true,  "Safe",       () => u.battle.exists( ! _.enemy.canAttack(u)))
    decide(true,  "Detonated",  () => u.unitClass.suicides && u.matchups.targetsInRange.exists(_.canAttack(u)))
    decide(false, "CantFight",  () => ! u.intent.canFight)
    decide(true,  "HugTank",    () => ! u.flying && u.agent.ride.isEmpty && With.enemies.exists(_.isTerran) && u.matchups.targetsInRange.exists(t => Terran.SiegeTankSieged(t) && t.visible && t.canAttack(u)))
    decide(true,  "Archon",     () => Protoss.Archon(u) && u.matchups.targetsInRange.exists(_.unitClass.attacksOrCastsOrDetectsOrTransports))
    decide(true,  "CoverFire",  () => u.matchups.targetNearest.exists(_.matchups.threatDeepestInRange.exists(_.isAny(IsTank, Terran.Bunker, Terran.MissileTurret, Protoss.Reaver, Protoss.PhotonCannon, Zerg.Guardian, IsBurrowedLurker, Zerg.SunkenColony, Zerg.SporeColony))))
    decide(true,  "Berzerk",    () => With.frame < berzerkCutoff && u.isAny(Protoss.Zealot, IsSlowling) && ! u.team.exists(_.catchesGround) && u.matchups.threatDeepest.exists(td => Terran.Vulture(td) && td.matchups.targetNearest.exists(tn => td.pixelsToGetInRange(tn) < 32 && tn.unitClass == u.unitClass)))
    decide(true,  "Cloaked",    () => u.effectivelyCloaked && ( ! Terran.Wraith(u) || u.energy > 10))
    decide(true,  "Cloakable",  () => Terran.Wraith(u) && Terran.WraithCloak() && u.energy >= 50  && u.matchups.groupVs.detectors.isEmpty)
    decide(true,  "Lurking",    () => Zerg.Lurker(u)                                              && u.matchups.groupVs.detectors.isEmpty)
    decide(true,  "Towers",     () => With.frame < Minutes(8)() && u.matchups.allies.exists(a => a.unitClass.isBuilding && a.unitClass.canAttack && (a.matchups.targetNearest.exists(t => u.canAttack(t) && a.inRangeToAttack(t)) || a.matchups.threatNearest.exists(t => u.canAttack(t) && t.canAttack(a) && t.framesToGetInRange(a) <= u.framesToGetInRange(a) && (t.pixelDistanceEdge(a) < 6 * 32) || t.inRangeToAttack(a)))))
    decide(false, "Scourge",    () => u.flying && With.enemies.exists(_.isZerg) && u.matchups.groupVs.has(Zerg.Scourge) && u.matchups.threats.exists(t => Zerg.Scourge(t) && t.pixelDistanceEdge(u) < 128 && t.matchups.threatsInRange.map(_.damageOnNextHitAgainst(t)).sum < t.hitPoints))
    decide(false, "Scarabless", () => Protoss.Reaver(u) && u.scarabs == 0 && ! u.trainee.exists(_.remainingCompletionFrames < Math.max(u.matchups.framesOfSafety, u.cooldownLeft)))
    decide(false, "Birdless",   () => Protoss.Carrier(u) && ! u.interceptors.exists(_.complete))
    decide(false, "NoEnergy",   () => ! u.canAttack && u.energyMax > 0 && u.unitClass.spells.forall(s => s.energyCost > u.energy || ! With.self.hasTech(s)))
    decide(false, "Disrupted",  () => ! u.flying && u.underDisruptionWeb && u.matchups.groupVs.canAttack(u))
    decide(false, "BideSiege",  () => Terran.SiegeTankUnsieged(u) && ! With.blackboard.wantToAttack()  && ! With.self.hasTech(Terran.SiegeMode) && u.matchups.groupVs.has(Protoss.Dragoon) && With.units.ours.exists(_.techProducing.contains(Terran.SiegeMode)) && u.alliesBattle.exists(a => Terran.Bunker(a) && a.complete))
    decide(false, "CarrierVuln",() => Protoss.Carrier(u) && u.matchups.threats.exists(t => ! t.flying && t.pixelsToGetInRange(u) < 32))
    decide(true,  "CarrierTrap",() => Protoss.Carrier(u) && u.matchups.groupOf.airToAirStrength >= u.matchups.groupVs.airToAirStrength || u.matchups.groupVs.has(Terran.Battlecruiser, Protoss.Carrier))
    decide(true,  "Raze",       () => ((IsTank(u) && Terran.SiegeMode()) || u.isAny(Protoss.Reaver, Zerg.Guardian) && u.matchups.threats.forall(t => t.unitClass.isWorker || t.unitClass.isBuilding)))
    decide(true,  "Miners",     () => ! u.isAny(IsWorker, Protoss.Carrier) && u.canAttack && u.battle.exists(_.us.minersThreatened) && u.presumptiveTarget.exists(t => t.isEnemyOf(u) && t.matchups.threateningMiners))

    decide(true,  "Energized",  () =>
      u.unitClass.maxShields > 20
      && With.frame < Minutes(10)()
      && u.alliesBattle.exists(ally =>
        Protoss.ShieldBattery(ally)
        && ally.complete
        && ally.energy > 20
        && ally.pixelDistanceEdge(u, u.presumptiveTarget.map(u.pixelToFireAtSimple).getOrElse(u.pixel)) < 72))

    if (decision.isDefined) {
      u.agent.shouldFight = decision.get
    } else {
      applyEstimation(u)
    }
  }
  
  private def applyEstimation(unit: FriendlyUnitInfo): Unit = {
    if (unit.battle.isEmpty) {
      // Important for things which care about unit willingness to trot around
      unit.agent.shouldFight = true
      unit.agent.fightReason = ""
    } else {
      unit.agent.shouldFight = unit.battle.get.judgement.get.unitShouldFight(unit)
      unit.agent.fightReason = ?(unit.agent.shouldFight, "Yes", "No")
    }
  }
}
