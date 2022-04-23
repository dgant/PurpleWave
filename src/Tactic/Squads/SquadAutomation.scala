package Tactic.Squads

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import Micro.Agency.Intention
import Micro.Formation.{Formation, FormationGeneric, FormationStyleDisengage, FormationStyleGuard}
import Utilities.UnitFilters.IsWarrior
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.Time.Minutes

import scala.collection.mutable.ArrayBuffer

object SquadAutomation {

  ///////////////
  // Targeting //
  ///////////////

  def target(squad: Squad): Unit = { target(squad, if (squad.fightConsensus) squad.vicinity else squad.homeConsensus) }
  def target(squad: Squad, to: Pixel): Unit = {
    squad.targets = Some(SquadAutomation.rankedEnRoute(squad, to))
  }
  def targetRaid(squad: Squad): Unit = targetRaid(squad, squad.vicinity)
  def targetRaid(squad: Squad, to: Pixel): Unit = {
    squad.targets = Some(
      Maff.orElse(
        SquadAutomation
          .rankForArmy(squad,
            unrankedEnRouteTo(squad, to).filter(t =>
                t.unitClass.isWorker
                || squad.units.exists(t.canAttack)
                || squad.units.exists(u => t.unitClass.canAttack(u) && t.inRangeToAttack(u))))
          .sortBy(t => ! t.unitClass.isWorker)
          .sortBy(t => - squad.units.count(_.inRangeToAttack(t))),
        SquadAutomation.rankForArmy(squad, to.base.map(_.units).getOrElse(Seq.empty).filter(_.isEnemy)))
      .toSeq)
  }

  def rankForArmy(squad: Squad, targets: Seq[UnitInfo]): Seq[UnitInfo] = {
    targets.sortBy(t =>
      (t.pixelDistanceCenter(squad.centroidKey)
      + (if (t.totalHealth < t.unitClass.maxTotalHealth) -16.0 else 0)
      + (16.0 * t.totalHealth / Math.max(1.0, t.unitClass.maxTotalHealth))
      + (if (t.unitClass.isWorker || squad.engagedUpon) 0 else 160)
      + 160)
      * (if (t.unitClass.attacksOrCastsOrDetectsOrTransports || ! squad.engagedUpon) 1 else 2))
  }
  def unrankedEnRouteTo(group: FriendlyUnitGroup, to: Pixel): Vector[UnitInfo] = {
    val combatEnemiesInRoute = With.units.enemy
      .filterNot(Protoss.Interceptor)
      .filter(e => if (e.flying) group.attacksAir else group.attacksGround)
      .filter(_.likelyStillThere)
      .filter(e => (e.canAttack || e.team.isDefined) && group.groupUnits.exists(u =>
        e.canAttack(u)
        && e.pixelsToGetInRange(u) < 32 * (if (u.visibleToOpponents && u.pixelDistanceTravelling(to) < e.pixelDistanceTravelling(to)) 1 else 8)))
      .toVector
    val combatTeams = combatEnemiesInRoute.flatMap(_.team).distinct
    val output =
      Maff.orElse(
        combatTeams.flatMap(_.units) ++ combatEnemiesInRoute.view.filter(_.team.isEmpty),
        // If there's no battle (defenseless targets) then wipe the zone!
        to.base.map(_.units.view.filter(_.isEnemy)).getOrElse(to.zone.units.view.filter(e => e.isEnemy && (if (e.flying) group.attacksAir else group.attacksGround)))).toVector
    output
  }
  def unrankedAround(group: FriendlyUnitGroup, to: Pixel): Vector[UnitInfo] = {
    With.units.enemy.filter(u => u.likelyStillThere && u.zone == to.zone || u.pixelDistanceCenter(to) < 32 * 12).toVector
  }

  def rankedEnRoute(squad: Squad): Seq[UnitInfo] = rankedEnRoute(squad, squad.vicinity)
  def rankedEnRoute(squad: Squad, goalAir: Pixel): Seq[UnitInfo] = rankForArmy(squad, unrankedEnRouteTo(squad, goalAir))
  def rankedAround(squad: Squad): Seq[UnitInfo] = rankedAround(squad, squad.vicinity)
  def rankedAround(squad: Squad, goalAir: Pixel): Seq[UnitInfo] = rankForArmy(squad, unrankedAround(squad, goalAir))

  ////////////////
  // Formations //
  ////////////////

  def form(squad: Squad, from: Pixel, to: Pixel): ArrayBuffer[Formation] = {
    var output: ArrayBuffer[Formation] = ArrayBuffer.empty
    // If advancing, give a formation for forward movement
    if (squad.fightConsensus) {
      val engageTarget = squad.targets.flatMap(_.headOption.map(_.pixel))
      output += FormationGeneric.march(squad, to)
    }
    // Always include a disengagey formation for units that want to retreat/kite
    if (squad.centroidKey.zone == squad.homeConsensus.zone && With.scouting.enemyThreatOrigin.zone != squad.homeConsensus.zone) {
      output += FormationGeneric.guard(squad, Some(squad.homeConsensus))
    } else {
      output += FormationGeneric.disengage(squad)
    }
    output
  }

  def send(squad: Squad, defaultReturn: Option[Pixel] = None): Unit = {
    squad.units.foreach(unit => {
      lazy val finalTravel = getTravel(unit, squad, defaultReturn)
      lazy val finalReturn = getReturn(unit, squad, defaultReturn)
      unit.intend(squad, new Intention {
        toTravel = if (squad.fightConsensus) finalTravel else finalReturn
        toReturn = finalReturn
      })
    })
  }

  def getReturn(unit: FriendlyUnitInfo, squad: Squad, defaultReturn: Option[Pixel] = None): Option[Pixel] = squad
    .formations
    .find(f => f.style == FormationStyleGuard || f.style == FormationStyleDisengage)
    .filter(_.placements.contains(unit))
    .map(_.placements(unit))
    .orElse(defaultReturn)
    .orElse(Some(squad.homeConsensus))

  def getTravel(unit: FriendlyUnitInfo, squad: Squad, defaultReturn: Option[Pixel] = None): Option[Pixel] =
    // Rush scenarios: Send army directly to vicinity
    if (With.frame < Minutes(5)() && ! With.units.existsEnemy(IsWarrior)) Some(squad.vicinity) else
    squad
    .formations
    .headOption
    .find(_.placements.contains(unit))
    .map(_.placements(unit))
    .orElse(if (squad.fightConsensus) Some(squad.vicinity) else getReturn(unit, squad, defaultReturn).orElse(Some(squad.homeConsensus)))

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
}
