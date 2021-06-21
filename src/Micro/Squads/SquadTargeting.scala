package Micro.Squads

import Information.Geography.Pathfinding.PathfindProfile
import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.Maff
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

import scala.collection.immutable.ListSet

object SquadTargeting {
  /*
    Ratio of path distance to (target combined distance from origin and goal)
    required to include a target as "on the way"
    This translates to 34 degree deviation from a straight line
   */
  private val distanceRatio = 1.2

  def enRouteTo(units: Iterable[FriendlyUnitInfo], goalAir: Pixel): Vector[UnitInfo] = {
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

  def rankEnRouteTo(units: Iterable[FriendlyUnitInfo], goalAir: Pixel): Seq[UnitInfo] = rankForArmy(units, enRouteTo(units, goalAir))

  def rankForArmy(units: Iterable[FriendlyUnitInfo], targets: Seq[UnitInfo]): Seq[UnitInfo] = {
    val centroid  = Maff.exemplar(units.view.map(_.pixel))
    val engaged   = units.exists(_.matchups.threatsInRange.nonEmpty)
    targets.sortBy(t =>
      t.pixelDistanceTravelling(centroid)
      + (32.0 * t.totalHealth / Math.max(1.0, t.unitClass.maxTotalHealth)) // Focus down weak units
      + (if (t.unitClass.attacksOrCastsOrDetectsOrTransports || ! engaged) 0 else With.mapPixelPerimeter))
  }
}
