package Micro.Coordination.Pathing

import Debugging.Visualizations.Forces
import Information.Geography.Pathfinding.Types.TilePath
import Information.Geography.Pathfinding.{PathfindProfile, PathfindRepulsor}
import Lifecycle.With
import Mathematics.Physics.{Force, ForceMath}
import Mathematics.Points.{Pixel, PixelRay}
import Mathematics.PurpleMath
import Mathematics.Shapes.Circle
import Micro.Actions.Combat.Maneuvering.DownhillPathfinder
import Micro.Agency.Commander
import Micro.Coordination.Pushing.Push
import Micro.Heuristics.Potential
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.{ByOption, TakeN}

import scala.collection.SeqView

object MicroPathing {

  def getSimplePath(unit: FriendlyUnitInfo, to: Option[Pixel] = None): TilePath = {
    val pathfindProfile               = new PathfindProfile(unit.tile)
    pathfindProfile.end               = to.orElse(unit.agent.toTravel).map(_.tile)
    pathfindProfile.employGroundDist  = true
    pathfindProfile.unit              = Some(unit)
    pathfindProfile.find
  }

  def getThreatAwarePath(unit: FriendlyUnitInfo, preferHome: Boolean = true): TilePath = {
    val pathLengthMinimum             = 7
    val pathfindProfile               = new PathfindProfile(unit.tile)
    pathfindProfile.end               = if (preferHome) Some(unit.agent.origin.tile) else None
    pathfindProfile.lengthMinimum     = Some(pathLengthMinimum)
    pathfindProfile.lengthMaximum     = Some(PurpleMath.clamp((unit.matchups.pixelsOfEntanglement + unit.effectiveRangePixels).toInt / 32, pathLengthMinimum, 15))
    pathfindProfile.threatMaximum     = Some(0)
    pathfindProfile.employGroundDist  = true
    pathfindProfile.costOccupancy     = if (unit.flying) 0f else 3f
    pathfindProfile.costRepulsion     = 9f
    pathfindProfile.repulsors         = getPathfindingRepulsors(unit)
    pathfindProfile.unit              = Some(unit)
    pathfindProfile.find
  }

  // Moving the wrong sorts of lengths can cause the unit to get stuck on obstacles.
  // For example, this is at 5 tiles: https://cdn.discordapp.com/attachments/421808419482370059/774666718793564220/unknown.png
  val waypointDistanceTiles: Int = 4
  val waypointDistancePixels: Int = 32 * waypointDistanceTiles
  private val ringDistance = waypointDistanceTiles * waypointDistanceTiles * 32 * 32
  def getCircleTowards(from: Pixel, to: Pixel): SeqView[Pixel, Seq[_]] = {
    Circle.points(5).view.map(p => from.add(p.x * 32, p.y * 32)).filter(_.valid)
  }

  def getWaypointToPixel(unit: UnitInfo, goal: Pixel): Pixel = {
    if (unit.flying) return goal
    val lineWaypoint      = if (PixelRay(unit.pixel, goal).forall(_.walkable)) Some(unit.pixel.project(goal, Math.min(unit.pixelDistanceCenter(goal), waypointDistancePixels))) else None
    lazy val hillPath     = DownhillPathfinder.decend(unit.tile, goal.tile)
    lazy val hillWaypoint = hillPath.map(path => path.last.center.add(unit.pixel.offsetFromTileCenter))
    lineWaypoint.orElse(hillWaypoint).getOrElse(goal)
  }

  def getWaypointAlongTilePath(path: TilePath): Option[Pixel] = {
    if (path.pathExists) Some(path.tiles.get.take(waypointDistanceTiles).last.center) else None
  }

  // More rays = more accurate movement, but more expensive
  // 8 is definitely too little for competent mvoement
  private def rayRadiansN(rays: Int) = (0 until rays).map(_ * 2 * Math.PI / rays - Math.PI).toVector.sortBy(Math.abs)
  private val rayRadians12 = rayRadiansN(12)
  private val rayRadians16 = rayRadiansN(16)
  private val rayRadians32 = rayRadiansN(32)
  def getWaypointInDirection(
    unit          : FriendlyUnitInfo,
    radians       : Double,
    mustApproach  : Option[Pixel] = None,
    requireSafety : Boolean = false,
    exactDistance : Option[Double] = None): Option[Pixel] = {

    val distance = exactDistance.getOrElse(waypointDistancePixels.toDouble)

    lazy val travelDistanceCurrent = mustApproach.map(unit.pixelDistanceTravelling)

    def acceptableForSafety(pixel: Pixel): Boolean = {
      ! requireSafety || unit.matchups.threats.forall(t => t.pixelDistanceSquared(pixel) > t.pixelDistanceSquared(unit.pixel))
    }

    if (mustApproach.exists(a => unit.pixelDistanceCenter(a) < distance && acceptableForSafety(a))) {
      return mustApproach
    }

    val rayStart = unit.pixel
    val waypointDistance = Math.max(distance, if (requireSafety) 64 + unit.matchups.pixelsOfEntanglement else 0)
    val rayRadians = if (With.reaction.sluggishness <= 1) rayRadians32 else if (With.reaction.sluggishness <= 2) rayRadians16 else rayRadians12
    val terminus = rayRadians
      .indices
      .view
      .map(i => {
        val deltaRadians = rayRadians(i)
        castRay(rayStart, lengthPixels = waypointDistance, radians = radians + deltaRadians, flying = unit.flying)
      })
      .find(p => {
        val terminus = p.clamp(unit.unitClass.dimensionMax / 2)
        lazy val towardsTerminus = unit.pixel.project(terminus, 8)
        lazy val travelDistanceTerminus = mustApproach.map(a => if (unit.flying) terminus.pixelDistance(a) else terminus.groundPixels(a))
        (
          terminus.pixelDistance(rayStart) >= 80
          && (travelDistanceTerminus.forall(_ < travelDistanceCurrent.get))
          && (acceptableForSafety(towardsTerminus)))
      })
    terminus
  }

  def tryMovingAlongTilePath(unit: FriendlyUnitInfo, path: TilePath): Unit = {
    val waypoint = getWaypointAlongTilePath(path).map(_.add(unit.pixel.offsetFromTileCenter))
    waypoint.foreach(pixel => {
      unit.agent.toTravel = waypoint
      Commander.move(unit)
    })
  }

  def getPathfindingRepulsors(unit: FriendlyUnitInfo, maxThreats: Int = 10): IndexedSeq[PathfindRepulsor] = {
    TakeN
      .by(maxThreats, unit.matchups.threats.view.filter(_.likelyStillThere))(Ordering.by(t => unit.pixelsOfEntanglement(t)))
      .map(threat => PathfindRepulsor(
        threat.pixel,
        threat.dpfOnNextHitAgainst(unit),
        64 + threat.pixelRangeAgainst(unit)))
      .toIndexedSeq
  }

  def setDefaultForces(unit: FriendlyUnitInfo, goalSafety: Boolean, goalHome: Boolean): Unit = {
    val to = unit.agent.origin

    // Where to go
    unit.agent.forces(Forces.threat)      = (Potential.avoidThreats(unit)     * PurpleMath.toInt(goalSafety))
    unit.agent.forces(Forces.travel)      = (Potential.preferTravel(unit, to) * PurpleMath.toInt(goalHome))
    unit.agent.forces(Forces.sneaking)    = (Potential.detectionRepulsion(unit))

    // How to get there
    unit.agent.forces(Forces.spreading)   = (Potential.preferSpreading(unit)  * PurpleMath.toInt(goalSafety))
    unit.agent.forces(Forces.regrouping)  = (Potential.preferRegrouping(unit) * PurpleMath.toInt( ! goalSafety))
    unit.agent.forces(Forces.spacing)     = (Potential.avoidCollision(unit))

    ForceMath.rebalance(unit.agent.forces, 1.5, Forces.threat, Forces.travel, Forces.sneaking)
    ForceMath.rebalance(unit.agent.forces, 1.0, Forces.spreading, Forces.regrouping, Forces.spacing)
  }

  def getPushForces(unit: FriendlyUnitInfo): Seq[(Push, Force)] = {
    With.coordinator.pushes.get(unit).map(p => (p, p.force(unit))).filter(_._2.exists(_.lengthSquared > 0)).map(p => (p._1, p._2.get))
  }

  def getPushRadians(pushForces: Seq[(Push, Force)]): Option[Double] = {
    val highestPriority = ByOption.max(pushForces.view.map(_._1.priority))
    pushForces
      .filter(pushForce => highestPriority.contains(pushForce._1.priority))
      .map(_._2)
      .reduceOption(_ + _)
      .map(_.radians)
  }

  def getPushRadians(unit: FriendlyUnitInfo): Option[Double] = getPushRadians(getPushForces(unit))

  def castRay(from: Pixel, lengthPixels: Double, radians: Double, flying: Boolean): Pixel = {
    var output = from
    var proceed = true
    PixelRay(from, lengthPixels = lengthPixels, radians = radians).foreach(tile =>
      if (proceed) {
        if (tile.valid && (flying || tile.walkableUnchecked)) {
          output = tile.center
        } else proceed = false
      }
    )
    output
  }
}
