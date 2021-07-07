package Tactics.Squads

import Information.Geography.Pathfinding.PathfindProfile
import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.Maff
import Micro.Agency.Intention
import Micro.Formation.FormationGeneric
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

import scala.collection.immutable.ListSet

object SquadAutomation {

  def targetAndSend(squad: Squad): Unit = targetAndSend(squad, squad.vicinity)
  def targetAndSend(squad: Squad, to: Pixel): Unit = targetAndSend(squad, squad.units, to)
  def targetAndSend(squad: Squad, units: Iterable[FriendlyUnitInfo], to: Pixel): Unit = {
    lazy val centroid             = Maff.centroid(units.view.map(_.pixel))
    lazy val fightConsensus       = Maff.mode(units.view.map(_.agent.shouldEngage))
    lazy val originConsensus      = Maff.mode(units.view.map(_.agent.defaultOrigin))
    lazy val battleConsensus      = Maff.mode(units.view.map(_.battle))
    lazy val targetReadyToEngage  = squad.targetQueue.get.find(t => units.exists(u => u.canAttack(t) && u.pixelsToGetInRange(t) < 64))
    lazy val targetHasEngagedUs   = squad.targetQueue.get.find(t => units.exists(u => t.canAttack(u) && t.inRangeToAttack(u)))
    if (fightConsensus) {
      squad.targetQueue = Some(SquadAutomation.rankForArmy(units, SquadAutomation.rankedEnRouteTo(units, to)))
      if (targetReadyToEngage.isDefined || targetHasEngagedUs.isDefined) {
        squad.formations += FormationGeneric.engage(units, targetReadyToEngage.orElse(targetHasEngagedUs).map(_.pixel))
      } else {
        squad.formations += FormationGeneric.march(units, to)
      }
    } else {
      squad.targetQueue = Some(SquadAutomation.rankForArmy(units, SquadAutomation.rankedEnRouteTo(units, originConsensus)))
      if (centroid.zone == originConsensus.zone && With.scouting.threatOrigin.zone != originConsensus.zone) {
        squad.formations += FormationGeneric.guard(units, Some(originConsensus))
      } else {
        squad.formations += FormationGeneric.disengage(units)
      }
    }

    units.foreach(unit => {
      unit.intend(this, new Intention {
        toTravel = squad.formations.find(_.placements.contains(unit)).map(_.placements(unit)).orElse(Some(to))
      })
    })
  }

  /*
    Ratio of path distance to (target combined distance from origin and goal)
    required to include a target as "on the way"
    This translates to 34 degree deviation from a straight line
   */
  private val distanceRatio = 1.2

  def unrankedEnRouteTo(units: Iterable[FriendlyUnitInfo], goalAir: Pixel): Vector[UnitInfo] = {
    val flying      = units.forall(_.flying)
    val antiAir     = units.exists(_.canAttackAir)
    val antiGround  = units.exists(_.canAttackGround)
    val originAir   = Maff.exemplar(units.view.map(_.pixel))
    val origin      = if (flying) originAir.tile else originAir.nearestWalkableTile
    val goal        = if (flying) goalAir.tile else goalAir.nearestWalkableTile
    val distancePx  = if (flying) origin.center.pixelDistance(goal.center) else origin.groundPixels(goal.center)
    val pathfind    = new PathfindProfile(origin, Some(goal), employGroundDist = true, canCrossUnwalkable = Some(flying), canEndUnwalkable = Some(flying))
    val path        = pathfind.find
    val zones       = new ListSet[Zone]() ++ (path.tiles.map(_.view.map(_.zone)).getOrElse(Seq.empty) ++ goal.metro.map(_.zones).getOrElse(Seq(goal.zone)))
    val output      = With.units.enemy
      .filter(e => if (e.flying) antiAir else antiGround)
      .filter(_.likelyStillThere)
      .filter(u => zones.contains(u.zone) || u.pixelDistanceTravelling(origin) + u.pixelDistanceTravelling(goal) < distanceRatio * distancePx).toVector
    output
  }

  def rankedEnRouteTo(units: Iterable[FriendlyUnitInfo], goalAir: Pixel): Seq[UnitInfo] = rankForArmy(units, unrankedEnRouteTo(units, goalAir))

  def rankForArmy(units: Iterable[FriendlyUnitInfo], targets: Seq[UnitInfo]): Seq[UnitInfo] = {
    val centroid  = Maff.exemplar(units.view.map(_.pixel))
    val engaged   = units.exists(_.matchups.threatsInRange.nonEmpty)
    targets.sortBy(t =>
      t.pixelDistanceSquared(centroid)
      - (if (t.totalHealth < t.unitClass.maxTotalHealth) -16.0 else 0) // Flat hysteresis: Focus units we've already started attacking
      + (32.0 * t.totalHealth / Math.max(1.0, t.unitClass.maxTotalHealth)) // Scaling hysteresis: Focus down weak units
      + (if (t.unitClass.attacksOrCastsOrDetectsOrTransports || ! engaged) 0 else With.mapPixelPerimeter))
  }


}
