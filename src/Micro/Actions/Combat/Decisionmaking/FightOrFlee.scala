
package Micro.Actions.Combat.Decisionmaking

import Lifecycle.With
import Mathematics.Maff
import Micro.Actions.Action
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.?
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
    decide(true,  "Irradiated", () => unit.irradiated && unit.unitClass.canBeIrradiateBurned)
    decide(true,  "Hug",        () => ! unit.flying && unit.matchups.targetsInRange.exists(t => unit.pixelDistanceEdge(t) < t.pixelRangeMin))
    decide(true,  "Safe",       () => unit.battle.isDefined && unit.matchups.threats.isEmpty)
    decide(true,  "Detonated",  () => unit.unitClass.suicides && unit.matchups.targets.exists(t => t.canAttack(unit) && t.matchups.targetsInRange.nonEmpty))
    decide(true,  "Archon",     () => Protoss.Archon(unit) && unit.matchups.targetsInRange.exists(_.unitClass.attacksOrCastsOrDetectsOrTransports))
    decide(false, "CantFight",  () => ! unit.intent.canFight)
    decide(true,  "Berzerk",    () => With.frame < Minutes(6)() && unit.isAny(Protoss.Zealot, Zerg.Zergling) && unit.metro.exists(_.bases.exists(_.owner.isEnemy)) && ! unit.team.exists(_.catchesGround) && unit.matchups.threats.exists(t => Terran.Vulture(t)))
    decide(true,  "Cloaked",    () => unit.effectivelyCloaked || (unit.is(Terran.Wraith) && unit.energy >= 50 && unit.matchups.enemyDetectors.isEmpty && With.self.hasTech(Terran.WraithCloak)))
    decide(true,  "Lurking",    () => Zerg.Lurker(unit) && unit.matchups.enemyDetectors.isEmpty)
    decide(false, "Scourge",    () => unit.flying && unit.matchups.threats.exists(t => Zerg.Scourge(t) && t.pixelDistanceEdge(unit) < 128 && t.matchups.threatsInRange.map(_.damageOnNextHitAgainst(t)).sum < t.hitPoints))
    decide(false, "Scarabs",    () => Protoss.Reaver(unit) && unit.scarabs == 0 && ! unit.trainee.exists(_.remainingCompletionFrames < Math.max(unit.matchups.framesOfSafety, unit.cooldownLeft)))
    decide(false, "Drained",    () => ! unit.canAttack && unit.energyMax > 0 && unit.unitClass.spells.forall(s => s.energyCost > unit.energy || ! With.self.hasTech(s)))
    decide(false, "Disrupted",  () => unit.underDisruptionWeb && ! unit.flying && unit.matchups.threats.exists(t => t.flying || ! t.underDisruptionWeb))
    decide(false, "BideSiege",  () => Terran.SiegeTankUnsieged(unit) && ! With.blackboard.wantToAttack() && unit.matchups.threats.exists(Protoss.Dragoon) && ! With.self.hasTech(Terran.SiegeMode) && With.units.ours.exists(_.techProducing.contains(Terran.SiegeMode)) && unit.alliesBattle.exists(a => Terran.Bunker(a) && a.complete))
    decide(false, "NoWorkers",  () => { val c = unit.matchups.threatsInRange.nonEmpty; With.frame < Minutes(6)() && unit.metro.exists(_.bases.exists(b => b.isOurs && b.workerCount > 3)) && With.units.ours.filter(IsWarrior).count(_.complete) < 5 && unit.matchups.threats.forall(_.unitClass.melee) && unit.matchups.allies.count(a => IsWorker(a) && a.pixelDistanceEdge(unit) < ?(c, 32, 72)) < 2 })
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
      unit.agent.shouldEngage = decision.get
    } else {
      applyEstimation(unit)
    }
  }
  
  private def applyEstimation(unit: FriendlyUnitInfo): Unit = {
    if (unit.battle.isEmpty) {
      // Important for things which care about unit willingness to trot around
      unit.agent.shouldEngage = true
      unit.agent.fightReason = ""
    } else {
      unit.agent.shouldEngage = unit.battle.get.judgement.get.unitShouldFight(unit)
      unit.agent.fightReason = if (unit.agent.shouldEngage) "Yes" else "No"
    }
  }
}
