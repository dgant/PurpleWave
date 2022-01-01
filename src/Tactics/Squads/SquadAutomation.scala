package Tactics.Squads

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import Micro.Agency.Intention
import Micro.Formation.{Formation, FormationGeneric, FormationStyleDisengage, FormationStyleGuard}
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

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
  // Ratio of path distance to (target combined distance from origin and goal) required to include a target as "on the way"
  // This equates to 34 degree deviation from a straight line
  val distanceRatio = 1.2
  def unrankedEnRouteTo(group: FriendlyUnitGroup, to: Pixel): Vector[UnitInfo] = {
    val units         = group.groupUnits
    val originAir     = group.homeConsensus
    val origin        = if (group.hasGround) originAir.nearestWalkableTile    else originAir.tile
    val goal          = if (group.hasGround) to.nearestWalkableTile           else to.tile
    val distancePx    = if (group.hasGround) origin.pixelDistanceGround(goal) else origin.pixelDistance(goal)
    val output        = With.units.enemy
      .filterNot(Protoss.Interceptor)
      .filter(e => if (e.flying) group.attacksAir else group.attacksGround)
      .filter(_.likelyStillThere)
      .filter(e =>
        e.pixelDistanceTravelling(origin) + e.pixelDistanceTravelling(goal) < distanceRatio * distancePx
        || e.presumptiveTarget.exists(t => units.contains(t) && e.inRangeToAttack(t)))
      .toVector
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
      if (engageTarget.isDefined && (squad.engagingOn || squad.engagedUpon)) {
        output += FormationGeneric.engage(squad, engageTarget)
      } else {
        output += FormationGeneric.march(squad, to)
      }
    }
    // Always include a disengagey formation for units that want to retreat/kite
    if (squad.centroidKey.zone == squad.homeConsensus.zone && With.scouting.threatOrigin.zone != squad.homeConsensus.zone) {
      output += FormationGeneric.guard(squad, Some(squad.homeConsensus))
    } else {
      output += FormationGeneric.disengage(squad)
    }
    output
  }

  def send(squad: Squad, toReturn: Option[Pixel] = None, minToForm: Int = 0): Unit = {
    if (squad.formations.isEmpty) {
      squad.units.foreach(_.intend(squad, new Intention { toTravel = Some(squad.vicinity); toReturn = toReturn }))
    } else {
      squad.units.foreach(unit => {
        unit.intend(squad, new Intention {
          toTravel = getTravel(unit, squad, toReturn, minToForm)
          toReturn = getReturn(unit, squad, toReturn, minToForm)
        })
      })
    }
  }

  def getReturn(unit: FriendlyUnitInfo, squad: Squad, defaultReturn: Option[Pixel] = None, minToForm: Int = 0): Option[Pixel] = squad
    .formations
    .find(f => f.style == FormationStyleGuard || f.style == FormationStyleDisengage)
    .filter(_.placements.size >= 2)
    .find(_.placements.contains(unit))
    .map(_.placements(unit))
    .orElse(defaultReturn)
    .orElse(Some(squad.homeConsensus))

  def getTravel(unit: FriendlyUnitInfo, squad: Squad, defaultReturn: Option[Pixel] = None, minToForm: Int = 0): Option[Pixel] = squad
    .formations
    .headOption
    .filter(_.placements.size >= minToForm)
    .find(_.placements.contains(unit))
    .map(_.placements(unit))
    .orElse(Some(if (squad.fightConsensus) squad.vicinity else getReturn(unit, squad, defaultReturn, minToForm).getOrElse(squad.homeConsensus)))

  //////////////////////
  // Full automation! //
  //////////////////////

  def targetAndSend(squad: Squad): Unit = targetAndSend(squad, from = squad.homeConsensus, to = squad.vicinity)
  def targetAndSend(squad: Squad, from: Pixel, to: Pixel): Unit = {
    target(squad)
    send(squad, Some(from))
  }

  def targetFormAndSend(squad: Squad): Unit = targetFormAndSend(squad, minToForm = 0)
  def targetFormAndSend(squad: Squad, minToForm: Int): Unit = targetFormAndSend(squad, from = squad.homeConsensus, to = squad.vicinity, minToForm = minToForm)
  def targetFormAndSend(squad: Squad, from: Pixel, to: Pixel, minToForm: Int): Unit = {
    target(squad)
    squad.formations = form(squad, from, to)
    send(squad, Some(from), minToForm = minToForm)
  }
}
