
package Micro.Actions.Combat.Decisionmaking

import Lifecycle.With
import Micro.Actions.Action
import Micro.Agency.AnchorMargin
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
    decide(true,  "Irradiated",   () => unit.irradiated && unit.unitClass.isOrganic)
    decide(true,  "CantFlee",     () => ! unit.agent.canFlee)
    decide(true,  "Hug",          () => ! unit.flying && unit.matchups.targetsInRange.exists(t => unit.pixelDistanceEdge(t) < t.pixelRangeMin))
    decide(true,  "Cloaked",      () => unit.effectivelyCloaked || (unit.is(Terran.Wraith) && unit.energy >= 50 && unit.matchups.enemyDetectors.isEmpty && With.self.hasTech(Terran.WraithCloak)))
    decide(false, "CantFight",    () => ! unit.agent.canFight)
    decide(false, "Tethered",     () => unit.matchups.anchor.exists(a => a.pixelDistanceEdge(unit) > a.effectiveRangePixels + AnchorMargin() && { val to = a.battle.map(_.enemy.vanguard()).getOrElse(With.scouting.threatOrigin.pixelCenter); unit.pixelDistanceTravelling(to) + a.effectiveRangePixels + AnchorMargin() < a.pixelDistanceCenter(to) }))
    decide(true,  "Safe",         () => unit.matchups.threats.isEmpty)
    decide(true,  "Berzerk",      () => With.frame < Minutes(6)() && unit.isAny(Protoss.Zealot, Zerg.Zergling) && unit.base.exists(b => b.owner.isEnemy || b.isNaturalOf.exists(_.owner.isEnemy)) && unit.matchups.threats.exists(t => t.is(Terran.Vulture) && t.matchups.catchers.isEmpty))
    decide(true,  "Lurking",      () => unit.is(Zerg.Lurker) && unit.matchups.enemyDetectors.isEmpty)
    decide(true,  "Detonated",    () => unit.isAny(Zerg.InfestedTerran, Zerg.Scourge) && unit.matchups.targets.exists(t => t.canAttack(unit) && t.matchups.targetsInRange.nonEmpty))
    decide(false, "Scarabs",      () => unit.is(Protoss.Reaver) && unit.scarabs == 0 && ! unit.trainee.exists(_.remainingCompletionFrames < Math.max(unit.matchups.framesOfSafety, unit.cooldownLeft)))
    decide(false, "Drained",      () => ! unit.canAttack && unit.energyMax > 0 && unit.unitClass.spells.forall(s => s.energyCost > unit.energy || ! With.self.hasTech(s)))
    decide(false, "Disrupted",    () => unit.underDisruptionWeb && ! unit.flying && unit.matchups.threats.exists(t => t.flying || ! t.underDisruptionWeb))
    decide(false, "BidingSiege",  () => unit.is(Terran.SiegeTankUnsieged) && ! With.blackboard.wantToAttack() && unit.matchups.threats.exists(Protoss.Dragoon) && With.units.ours.exists(_.techProducing.contains(Terran.SiegeMode)) && unit.alliesBattle.exists(a => a.is(Terran.Bunker) && a.complete))
    decide(true,  "Energized", () =>
      unit.unitClass.maxShields > 10
      && unit.alliesBattle.exists(ally =>
        ally.is(Protoss.ShieldBattery)
        && ally.complete
        && ally.energy > 20
        && ally.pixelDistanceEdge(unit, otherAt = ByOption.minBy(unit.matchups.targets.view.map(unit.pixelToFireAt))(unit.pixelDistanceCenter).getOrElse(unit.pixel)) < 72))

    decide(true, "Anchors", () => unit.matchups.anchors.exists(anchor =>
        anchor.visibleToOpponents && anchor.matchups.pixelsOfEntanglement > unit.matchups.pixelsOfEntanglement - AnchorMargin()))

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
