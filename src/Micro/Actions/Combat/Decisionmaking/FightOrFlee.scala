
package Micro.Actions.Combat.Decisionmaking

import Lifecycle.With
import Mathematics.Maff
import Micro.Actions.Action
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.Time.Minutes
import Utilities.UnitFilters.{IsTank, IsWarrior, IsWorker}

object FightOrFlee extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.canMove

  override def perform(unit: FriendlyUnitInfo): Unit = {

    var decision: Option[Boolean] = None

    def decide(shouldEngage: Boolean, description: String, condition: () => Boolean): Unit = {
      if (decision.isEmpty && condition()) {
        decision = Some(shouldEngage)
        unit.agent.fightReason = description
      }
    }

    decide(true,  "Static",     () => ! unit.canMove)
    decide(true,  "CantFlee",   () => ! unit.intent.canFlee)
    decide(true,  "Committed",  () => unit.agent.commit)
    decide(true,  "YOLO",       () => With.yolo.active && IsWarrior(unit))
    decide(true,  "Irradiated", () => unit.painfullyIrradiated)
    decide(true,  "Safe",       () => unit.battle.isDefined && unit.matchups.threats.isEmpty)
    decide(true,  "Detonated",  () => unit.unitClass.suicides && unit.matchups.targetsInRange.exists(_.canAttack(unit)))
    decide(true,  "Hug",        () => unit.agent.ride.isEmpty && unit.matchups.targetsInRange.exists(t => Terran.SiegeTankSieged(t) && t.visible && t.canAttack(unit)))
    decide(true,  "Archon",     () => Protoss.Archon(unit) && unit.matchups.targetsInRange.exists(_.unitClass.attacksOrCastsOrDetectsOrTransports))
    decide(false, "CantFight",  () => ! unit.intent.canFight)
    decide(true,  "Berzerk",    () => With.frame < Minutes(6)() && unit.isAny(Protoss.Zealot, Zerg.Zergling) && ! Zerg.ZerglingSpeed() && ! unit.metro.exists(_.isOurs) && ! unit.team.exists(_.catchesGround) && unit.matchups.threats.exists(Terran.Vulture))
    decide(true,  "Cloaked",    () => unit.effectivelyCloaked || (unit.is(Terran.Wraith) && unit.energy >= 50 && unit.matchups.groupVs.detectors.isEmpty && With.self.hasTech(Terran.WraithCloak)))
    decide(true,  "Lurking",    () => Zerg.Lurker(unit) && unit.matchups.groupVs.detectors.isEmpty)
    decide(false, "Scourge",    () => unit.flying && unit.matchups.threats.exists(t => Zerg.Scourge(t) && t.pixelDistanceEdge(unit) < 128 && t.matchups.threatsInRange.map(_.damageOnNextHitAgainst(t)).sum < t.hitPoints))
    decide(false, "Scarabless", () => Protoss.Reaver(unit) && unit.scarabs == 0 && ! unit.trainee.exists(_.remainingCompletionFrames < Math.max(unit.matchups.framesOfSafety, unit.cooldownLeft)))
    decide(false, "Birdless",   () => Protoss.Carrier(unit) && ! unit.interceptors.exists(_.complete))
    decide(false, "NoEnergy",   () => ! unit.canAttack && unit.energyMax > 0 && unit.unitClass.spells.forall(s => s.energyCost > unit.energy || ! With.self.hasTech(s)))
    decide(false, "Disrupted",  () => unit.underDisruptionWeb && ! unit.flying && unit.matchups.threats.exists(t => t.flying || ! t.underDisruptionWeb))
    decide(false, "BideSiege",  () => Terran.SiegeTankUnsieged(unit) && ! With.blackboard.wantToAttack() && unit.matchups.threats.exists(Protoss.Dragoon) && ! With.self.hasTech(Terran.SiegeMode) && With.units.ours.exists(_.techProducing.contains(Terran.SiegeMode)) && unit.alliesBattle.exists(a => Terran.Bunker(a) && a.complete))
    decide(false, "CarrierVuln",() => Protoss.Carrier(unit) && unit.matchups.threats.filterNot(_.flying).exists(_.pixelsToGetInRange(unit) < 32))
    decide(true,  "CarrierTrap",() => Protoss.Carrier(unit) && unit.matchups.groupOf.airToAirStrength >= unit.matchups.groupVs.airToAirStrength || unit.matchups.threats.exists(_.isAny(Terran.Battlecruiser, Protoss.Carrier)))
    decide(true,  "Workers",    () => unit.matchups.targets.exists(_.canAttackGround) && unit.matchups.allies.flatMap(_.friendly).exists(a => IsWorker(a) && (a.matchups.targetsInRange ++ a.orderTarget).exists(t => t.isEnemy && a.framesToGetInRange(t) <= 4 + unit.framesToGetInRange(t))))
    decide(true,  "Raze",       () => ((IsTank(unit) && Terran.SiegeMode()) || unit.isAny(Protoss.Reaver, Zerg.Guardian) && unit.matchups.threats.forall(t => t.unitClass.isWorker || t.unitClass.isBuilding)))
    decide(true,  "Energized",  () =>
      unit.unitClass.maxShields > 20
      && With.frame < Minutes(10)()
      && unit.alliesBattle.exists(ally =>
        Protoss.ShieldBattery(ally)
        && ally.complete
        && ally.energy > 20
        && ally.pixelDistanceEdge(unit, otherAt = Maff.minBy(unit.matchups.targets.view.map(unit.pixelToFireAt))(unit.pixelDistanceCenter).getOrElse(unit.pixel)) < 72))
    if (decision.isDefined) {
      unit.agent.shouldFight = decision.get
    } else {
      applyEstimation(unit)
    }
  }
  
  private def applyEstimation(unit: FriendlyUnitInfo): Unit = {
    if (unit.battle.isEmpty) {
      // Important for things which care about unit willingness to trot around
      unit.agent.shouldFight = true
      unit.agent.fightReason = ""
    } else {
      unit.agent.shouldFight = unit.battle.get.judgement.get.unitShouldFight(unit)
      unit.agent.fightReason = if (unit.agent.shouldFight) "Yes" else "No"
    }
  }
}
