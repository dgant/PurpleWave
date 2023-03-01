package Tactic.Squads

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import Micro.Formation.{Formation, FormationStyleDisengage, FormationStyleGuard, Formations}
import Micro.Targeting.FiltersRequired.TargetFilterFocus
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.?
import Utilities.Time.Minutes
import Utilities.UnitFilters.IsWorker

import scala.collection.mutable.ArrayBuffer

object SquadAutomation {

  ///////////////
  // Targeting //
  ///////////////

  def target(squad: Squad): Unit = target(squad, ?(squad.fightConsensus, squad.vicinity, squad.homeConsensus))
  def target(squad: Squad, to: Pixel): Unit = squad.setTargets(SquadAutomation.rankedEnRoute(squad, to))

  def targetRaid(squad: Squad): Unit = targetRaid(squad, squad.vicinity)
  def targetRaid(squad: Squad, to: Pixel): Unit = {
    val unrankedTargets   = unrankedAround(squad, to)
    lazy val targetCombat = unrankedTargets.filter(t => t.unitClass.isWorker || squad.canBeAttackedBy(t) || (t.unitClass.isStaticDefense && squad.units.exists(t.inRangeToAttack)))
    lazy val targetHall   = unrankedTargets.filter(_.unitClass.isTownHall)
    squad.setTargets(SquadAutomation.rankForArmy(squad, Maff.orElse(targetCombat, targetHall, unrankedTargets).toVector).sortBy( ! _.unitClass.isWorker))
  }

  def rankForArmy(squad: Squad, targets: Vector[UnitInfo]): Vector[UnitInfo] = {
    targets.sortBy(t =>
        ?(t.visible,                                  0, 32 * 5)
      + ?(t.pixel == t.pixelObserved,                 0, 32 * 5)
      + ?(t.unitClass.isWorker || squad.engagedUpon,  0, 32 * 5)
      + t.pixelDistanceCenter(squad.centroidKey)
      + t.pixelDistanceCenter(squad.vicinity))
  }
  def unrankedEnRouteTo(group: FriendlyUnitGroup, to: Pixel): Set[UnitInfo] = {
    val combatEnemiesInRoute = group.battleEnemies.filter(e =>
      ! Protoss.Interceptor(e)
      && e.likelyStillThere
      && group.canAttack(e)
      && (
        TargetFilterFocus.canTargetAsRoadblock(group, e)
        || group.meanAttackerSpeed > 1.2 * e.topSpeed))
    val around = unrankedAround(group, to)
    combatEnemiesInRoute ++ around
  }
  def unrankedAround(group: FriendlyUnitGroup, to: Pixel): Vector[UnitInfo] = {
    to.base.getOrElse(to.zone).enemies
      .filter(_.likelyStillThere)
      .filter(group.canAttack)
      .toVector
  }

  def rankedEnRoute(squad: Squad): Seq[UnitInfo] = rankedEnRoute(squad, squad.vicinity)
  def rankedEnRoute(squad: Squad, goalAir: Pixel): Seq[UnitInfo] = rankForArmy(squad, unrankedEnRouteTo(squad, goalAir).toVector)
  def rankedAround(squad: Squad): Seq[UnitInfo] = rankedAround(squad, squad.vicinity)
  def rankedAround(squad: Squad, goalAir: Pixel): Seq[UnitInfo] = rankForArmy(squad, unrankedAround(squad, goalAir))

  ////////////////
  // Formations //
  ////////////////

  def form(squad: Squad, from: Pixel, to: Pixel): ArrayBuffer[Formation] = {
    val output: ArrayBuffer[Formation] = ArrayBuffer.empty
    // If advancing, give a formation for forward movement
    if (squad.fightConsensus) {
      output += Formations.march(squad, to)
    }
    // Always include a disengagey formation for units that want to retreat/kite
    if (squad.centroidKey.zone == squad.homeConsensus.zone && With.scouting.enemyThreatOrigin.zone != squad.homeConsensus.zone) {
      output += Formations.guard(squad, Some(squad.homeConsensus))
    } else {
      output += Formations.disengage(squad)
    }
    output
  }

  def send(squad: Squad, defaultReturn: Option[Pixel] = None): Unit = {
    sendUnits(squad, squad.unintended, defaultReturn)
  }

  def sendUnits(squad: Squad, units: Iterable[FriendlyUnitInfo], defaultReturn: Option[Pixel] = None): Unit = {
    units.foreach(unit => {
      lazy val finalTravel = getTravel(unit, squad, defaultReturn)
      lazy val finalReturn = getReturn(unit, squad, defaultReturn)
      unit.intend(squad)
        .setTravel(?(squad.fightConsensus, finalTravel, finalReturn))
        .setReturnTo(finalReturn)
    })
  }

  def getReturn(unit: FriendlyUnitInfo, squad: Squad, defaultReturn: Option[Pixel] = None): Pixel = squad
    .formations
    .find(f => f.style == FormationStyleGuard || f.style == FormationStyleDisengage)
    .filter(_.placements.contains(unit))
    .map(_.placements(unit))
    .orElse(defaultReturn)
    .getOrElse(squad.homeConsensus)

  def getTravel(unit: FriendlyUnitInfo, squad: Squad, defaultReturn: Option[Pixel] = None): Pixel =
    // Rush scenarios: Send army directly to vicinity
    Some(squad.vicinity).filter(p => With.frame < Minutes(5)() && ! p.zone.metro.exists(_.bases.exists(_.isOurs)) && unit.matchups.threats.forall(IsWorker))
      .orElse(?(squad.hasGround, None, Some(squad.vicinity)))
      .orElse(
        squad
          .formations
          .headOption
          .find(_.placements.contains(unit))
          .map(_.placements(unit)))
      .getOrElse(?(squad.fightConsensus, squad.vicinity, getReturn(unit, squad, defaultReturn)))

  //////////////////////
  // Full automation! //
  //////////////////////

  def targetAndSend(squad: Squad): Unit = targetAndSend(squad, from = squad.homeConsensus, to = squad.vicinity)
  def targetAndSend(squad: Squad, from: Pixel, to: Pixel): Unit = {
    target(squad)
    send(squad, defaultReturn = Some(from))
  }

  def targetFormAndSend(squad: Squad): Unit = targetFormAndSend(squad, from = squad.homeConsensus, to = squad.vicinity)
  def targetFormAndSend(squad: Squad, from: Pixel, to: Pixel): Unit = {
    target(squad)
    squad.formations = form(squad, from, to)
    send(squad, defaultReturn = Some(from))
  }

  def formAndSend(squad: Squad): Unit = formAndSend(squad, from = squad.homeConsensus, to = squad.vicinity)
  def formAndSend(squad: Squad, from: Pixel, to: Pixel): Unit = {
    squad.formations = form(squad, from, to)
    send(squad, defaultReturn = Some(from))
  }
}
