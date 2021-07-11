package Tactics.Squads

import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Agency.Intention
import Micro.Formation.{Formation, FormationGeneric}
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable.ArrayBuffer

object SquadAutomation {

  ///////////////
  // Targeting //
  ///////////////

  def target(squad: Squad): Unit = { target(squad, if (squad.fightConsensus) squad.vicinity else squad.homeConsensus) }
  def target(squad: Squad, to: Pixel): Unit = {
    squad.targetQueue = Some(SquadAutomation.rankedEnRoute(squad, to))
  }
  def rankForArmy(group: UnitGroup, targets: Seq[UnitInfo]): Seq[UnitInfo] = {
    targets.sortBy(t =>
      (t.pixelDistanceCenter(group.centroidKey)
      + (if (t.totalHealth < t.unitClass.maxTotalHealth) -16.0 else 0)
      + (16.0 * t.totalHealth / Math.max(1.0, t.unitClass.maxTotalHealth))
      + 160)
      * (if (t.unitClass.attacksOrCastsOrDetectsOrTransports || ! group.engagedUpon) 1 else 2))
  }
  def unrankedTargetsTo(group: FriendlyUnitGroup, to: Pixel): Vector[UnitInfo] = {
    // Ratio of path distance to (target combined distance from origin and goal) required to include a target as "on the way"
    // This equates to 34 degree deviation from a straight line
    val distanceRatio = 1.2
    val units         = group.groupUnits
    val originAir     = group.homeConsensus
    val origin        = if (group.hasGround) originAir.nearestWalkableTile    else originAir.tile
    val goal          = if (group.hasGround) to.nearestWalkableTile           else to.tile
    val distancePx    = if (group.hasGround) origin.pixelDistanceGround(goal) else origin.pixelDistance(goal)
    val output        = With.units.enemy
      .filter(e => if (e.flying) group.attacksAir else group.attacksGround)
      .filter(_.likelyStillThere)
      .filter(e =>
        e.pixelDistanceTravelling(origin) + e.pixelDistanceTravelling(goal) < distanceRatio * distancePx
        || e.presumptiveTarget.exists(t => units.contains(t) && e.inRangeToAttack(t)))
      .toVector
    output
  }
  def rankedEnRoute(squad: Squad): Seq[UnitInfo] = rankedEnRoute(squad, squad.vicinity)
  def rankedEnRoute(group: FriendlyUnitGroup, goalAir: Pixel): Seq[UnitInfo] = rankForArmy(group, unrankedTargetsTo(group, goalAir))

  ////////////////
  // Formations //
  ////////////////

  def form(squad: Squad, from: Pixel, to: Pixel): ArrayBuffer[Formation] = {
    var output: ArrayBuffer[Formation] = ArrayBuffer.empty
    // If advancing, give a formation for forward movement
    if (squad.fightConsensus) {
      val engageTarget = squad.targetQueue.flatMap(_.headOption.map(_.pixel))
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

  def send(squad: Squad, minToForm: Int = 0): Unit = {
    squad.units.foreach(unit => {
      unit.intend(this, new Intention {
        // Send to the first formation, which will be advancey if we're advancing
        toTravel = squad
          .formations
          .headOption
          .filter(_.placements.size >= minToForm)
          .find(_.placements.contains(unit))
          .map(_.placements(unit))
          .orElse(Some(if (squad.fightConsensus) squad.vicinity else squad.homeConsensus))
        // The last formation is the most retreaty formation
        // If we only have one, let units retreat to their own origin
        toReturn = squad
            .formations
            .view
            .drop(1)
            .lastOption
            .filter(_.placements.size >= minToForm)
            .find(_.placements.contains(unit))
            .map(_.placements(unit))
      })
    })
  }

  //////////////////////
  // Full automation! //
  //////////////////////

  def targetFormAndSend(squad: Squad): Unit = targetFormAndSend(squad, minToForm = 0)
  def targetFormAndSend(squad: Squad, minToForm: Int): Unit = targetFormAndSend(squad, from = squad.homeConsensus, to = squad.vicinity, minToForm = minToForm)
  def targetFormAndSend(squad: Squad, from: Pixel, to: Pixel, minToForm: Int): Unit = {
    target(squad)
    squad.formations = form(squad, from, to)
    send(squad, minToForm = minToForm)
  }
}
