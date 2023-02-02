package Micro.Coordination.Pathing

import Debugging.Visualizations.Forces
import Information.Geography.Pathfinding.Types.TilePath
import Information.Geography.Pathfinding.{PathfindProfile, PathfindRepulsor}
import Lifecycle.With
import Mathematics.Physics.Force
import Mathematics.Points.{Pixel, Tile}
import Mathematics.Shapes.{Circle, Ray}
import Mathematics.{Maff, Shapes}
import Micro.Actions.Combat.Maneuvering.DownhillPathfinder
import Micro.Agency.Commander
import Micro.Coordination.Pushing.Push
import Micro.Heuristics.Potential
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.?

import scala.collection.SeqView

object MicroPathing {

  def getSimplePath(unit: FriendlyUnitInfo, to: Option[Tile] = None): TilePath = {
    val pathfindProfile               = new PathfindProfile(unit.tile)
    pathfindProfile.end               = to.orElse(unit.agent.toTravel.map(_.tile))
    pathfindProfile.employGroundDist  = ! unit.flying
    pathfindProfile.unit              = Some(unit)
    pathfindProfile.find
  }

  def getSneakyPath(unit: FriendlyUnitInfo, to: Option[Tile] = None): TilePath = {
    val pathfindProfile               = new PathfindProfile(unit.tile)
    pathfindProfile.end               = to.orElse(unit.agent.toTravel.map(_.tile))
    pathfindProfile.employGroundDist  = ! unit.flying
    pathfindProfile.unit              = Some(unit)
    pathfindProfile.costEnemyVision   = 5
    pathfindProfile.costThreat        = 25
    pathfindProfile.lengthMaximum     = Some(48)
    pathfindProfile.find
  }

  def getThreatAwarePath(unit: FriendlyUnitInfo, preferHome: Boolean = true): TilePath = {
    val pathLengthMinimum             = 7
    val pathfindProfile               = new PathfindProfile(unit.tile)
    pathfindProfile.end               = if (preferHome) Some(unit.agent.safety.tile) else None
    pathfindProfile.lengthMinimum     = Some(pathLengthMinimum)
    pathfindProfile.lengthMaximum     = Some(Maff.clamp((unit.matchups.pixelsEntangled + unit.effectiveRangePixels).toInt / 32, pathLengthMinimum, 15))
    pathfindProfile.threatMaximum     = Some(0)
    pathfindProfile.employGroundDist  = true
    pathfindProfile.costOccupancy     = if (unit.flying) 0 else 3
    pathfindProfile.costRepulsion     = 3
    pathfindProfile.costThreat        = 6
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
    Circle(5).view.map(p => from.add(p.x * 32, p.y * 32)).filter(_.valid)
  }

  def getWaypointToPixel(unit: UnitInfo, goal: Pixel): Pixel = ?(unit.flying, goal, getGroundWaypointToPixel(unit.pixel, goal))

  def getGroundWaypointToPixel(from: Pixel, rawGoal: Pixel): Pixel = {
    if (from.pixelDistance(rawGoal) < 32) return rawGoal
    val goal              = rawGoal.walkablePixel
    val lineWaypoint      = if (Ray(from, goal).forall(_.walkable)) Some(from.project(goal, Math.min(from.pixelDistance(goal), waypointDistancePixels))) else None
    lazy val hillPath     = DownhillPathfinder.decend(from.tile, goal.tile)
    lazy val hillWaypoint = hillPath.map(path => path.last.center.add(from.offsetFromTileCenter))
    lineWaypoint.orElse(hillWaypoint).getOrElse(goal)
  }

  def getWaypointAlongTilePath(unit: FriendlyUnitInfo, path: TilePath): Option[Pixel] = {
    if (path.pathExists) {
      val currentIndex = path.tiles.get.indices.minBy(i => path.tiles.get(i).center.pixelDistanceSquared(unit.pixel))
      Some(path.tiles.get.take(currentIndex + waypointDistanceTiles).last.center)
    } else None
  }

  // More rays = more accurate movement, but more expensive
  // 8 is definitely too little for competent movement
  private def rayRadiansN(rays: Int) = (0 until rays).map(_ * 2 * Math.PI / rays - Math.PI).toVector.sortBy(Math.abs)
  private val rayRadians12 = rayRadiansN(12)
  private val rayRadians16 = rayRadiansN(16)
  private val rayRadians32 = rayRadiansN(32)
  def getWaypointInDirection(
    unit          : FriendlyUnitInfo,
    radians       : Double,
    mustApproach  : Option[Pixel] = None,
    requireSafety : Boolean = false): Option[Pixel] = {

    lazy val travelDistanceCurrent = mustApproach.map(unit.pixelDistanceTravelling)

    val enemyRangeNow = unit.tile.enemyRangeAgainst(unit)
    def acceptableForSafety(pixel: Pixel): Boolean = ! requireSafety || pixel.tile.enemyRangeAgainst(unit) <= enemyRangeNow

    if (mustApproach.exists(a => unit.pixelDistanceCenter(a) < waypointDistancePixels && acceptableForSafety(a))) return mustApproach

    val waypointDistance = Math.max(waypointDistancePixels, if (requireSafety) 64 + unit.matchups.pixelsEntangled else 0)
    val rayRadians = if (With.reaction.sluggishness <= 1) rayRadians32 else if (With.reaction.sluggishness <= 2) rayRadians16 else rayRadians12
    val terminus = rayRadians
      .indices
      .view
      .map(i => castRay(unit.pixel, lengthPixels = waypointDistance, radians = radians + rayRadians(i), flying = unit.flying))
      .find(p => {
        val clamped = p.clamp(unit.unitClass.dimensionMax / 2)
        (unit.pixelDistanceCenter(clamped) >= 80
          && travelDistanceCurrent.forall(_ > clamped.travelPixelsFor(mustApproach.get, unit))
          && acceptableForSafety(unit.pixel.project(clamped, 80)))
      })
    terminus
  }

  def tryMovingAlongTilePath(unit: FriendlyUnitInfo, path: TilePath): Unit = {
    val waypoint = getWaypointAlongTilePath(unit, path).map(_.add(unit.pixel.offsetFromTileCenter))
    waypoint.foreach(pixel => {
      unit.agent.toTravel = waypoint
      Commander.move(unit)
    })
  }

  def getPathfindingRepulsors(unit: FriendlyUnitInfo, maxThreats: Int = 10): IndexedSeq[PathfindRepulsor] = {
    Maff
      .takeN(maxThreats, unit.matchups.threats.view.filter(_.likelyStillThere))(Ordering.by(t => unit.pixelsOfEntanglement(t)))
      .map(threat => PathfindRepulsor(
        threat.pixel,
        threat.dpfOnNextHitAgainst(unit),
        64 + threat.pixelRangeAgainst(unit)))
      .toIndexedSeq
  }

  def setDefaultForces(unit: FriendlyUnitInfo, goalSafety: Boolean, goalOrigin: Boolean): Unit = {
    unit.agent.forces(Forces.sneaking)  = Potential.avoidDetection(unit)
    unit.agent.forces(Forces.travel)    = Potential.towards(unit, unit.agent.safety)  * Maff.toInt(goalOrigin)
    unit.agent.forces(Forces.threat)    = Potential.hardAvoidThreatRange(unit)        * Maff.toInt(goalSafety)
    if (unit.agent.forces.forall(_._2.lengthSquared == 0)) {
      unit.agent.forces(Forces.travel)  = Potential.towards(unit, unit.agent.destination)
    }
    unit.agent.forces(Forces.spacing) = Potential.preferSpacing(unit)
    unit.agent.forces(Forces.pushing) = Potential.followPushes(unit)
  }

  def getPushForces(unit: FriendlyUnitInfo): Seq[(Push, Force)] = {
    With.coordinator.pushes.get(unit).map(p => (p, p.force(unit))).filter(_._2.exists(_.lengthSquared > 0)).map(p => (p._1, p._2.get))
  }

  def getPushRadians(pushForces: Seq[(Push, Force)]): Option[Double] = {
    val highestPriority = Maff.max(pushForces.view.map(_._1.priority))
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
    Shapes.Ray(from, lengthPixels = lengthPixels, radians = radians).foreach(tile =>
      if (proceed) {
        if (tile.valid && (flying || tile.walkableUnchecked)) {
          output = tile.center
        } else proceed = false
      }
    )
    output
  }
}
