package Micro.Coordination.Pathing

import Debugging.Visualizations.Forces
import Information.Geography.Pathfinding.Types.TilePath
import Information.Geography.Pathfinding.{PathfindProfile, PathfindRepulsor}
import Lifecycle.With
import Mathematics.Physics.Force
import Mathematics.Points.{Pixel, Points, Tile}
import Mathematics.Shapes.{Circle, Ray}
import Mathematics.{Maff, Shapes}
import Micro.Actions.Combat.Maneuvering.DownhillPathfinder
import Micro.Agency.Commander
import Micro.Coordination.Pushing.Push
import Micro.Heuristics.Potential
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.{?, SomeIf}

import scala.collection.SeqView

object MicroPathing {

  def getSimplePath(unit: FriendlyUnitInfo, to: Option[Tile] = None): TilePath = {
    val pathfindProfile               = new PathfindProfile(unit.tile)
    pathfindProfile.end               = to.orElse(unit.agent.destinationNext.pixel.map(_.tile))
    pathfindProfile.employGroundDist  = ! unit.flying
    pathfindProfile.unit              = Some(unit)
    pathfindProfile.find
  }

  def getSneakyPath(unit: FriendlyUnitInfo, to: Option[Tile] = None): TilePath = {
    val pathfindProfile               = new PathfindProfile(unit.tile)
    pathfindProfile.end               = to.orElse(unit.agent.destinationNext.pixel.map(_.tile))
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
    pathfindProfile.end               = SomeIf(preferHome, unit.agent.safety.tile)
    pathfindProfile.lengthMinimum     = Some(pathLengthMinimum)
    pathfindProfile.lengthMaximum     = Some(Maff.clamp((unit.matchups.pixelsEntangled + unit.effectiveRangePixels).toInt / 32, pathLengthMinimum, 15))
    pathfindProfile.threatMaximum     = Some(0)
    pathfindProfile.employGroundDist  = true
    pathfindProfile.costOccupancy     = ?(unit.flying, 0, 3)
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
    val lineWaypoint      = SomeIf(Ray(from, goal).forall(_.walkable), from.project(goal, Math.min(from.pixelDistance(goal), waypointDistancePixels)))
    lazy val hillPath     = DownhillPathfinder.decend(from.tile, goal.tile)
    lazy val hillWaypoint = hillPath.map(path => path.last.center.add(from.offsetFromTileCenter))
    lineWaypoint.orElse(hillWaypoint).getOrElse(goal)
  }

  def getWaypointAlongTilePath(unit: FriendlyUnitInfo, path: TilePath): Option[Pixel] = {
    SomeIf(
      path.pathExists, {
        val currentIndex = path.tiles.get.indices.minBy(i => path.tiles.get(i).center.pixelDistanceSquared(unit.pixel))
        path.tiles.get.take(currentIndex + waypointDistanceTiles).last.center
      })
  }

  def moveForcefully(unit: FriendlyUnitInfo): Unit = {
    setWaypointForcefully(unit)
    Commander.move(unit)
  }

  def setWaypointForcefully(unit: FriendlyUnitInfo): Boolean = {
    val radians = MicroPathing
      .getPushRadians(unit)
      .getOrElse(unit.agent.forces.sum.radians)
    val waypoint = MicroPathing.getWaypointInDirection(unit, radians)
    unit.agent.forced.setAsWaypoint(waypoint)
    waypoint.isDefined
  }

  // More rays = more accurate movement, but more expensive
  // 8 is definitely too little for competent movement
  private def rayRadiansN(rays: Int) = (0 until rays).map(_ * 2 * Math.PI / rays - Math.PI).toVector.sortBy(Math.abs)
  private val rayRadians32 = rayRadiansN(32)
  private val rayRadians16 = rayRadiansN(16)
  private val rayRadians12 = rayRadiansN(12)
  def getWaypointInDirection(
    unit          : FriendlyUnitInfo,
    radians       : Double,
    mustApproach  : Option[Pixel] = None,
    requireSafety : Boolean = false): Option[Pixel] = {

    lazy val unacceptableDistance = mustApproach.map(unit.pixelDistanceTravelling)

    def acceptableForSafety(pixel: Pixel): Boolean = ! requireSafety || pixel.tile.enemyRangeAgainst(unit) < Math.max(1, unit.tile.enemyRangeAgainst(unit))

    if (mustApproach.exists(a => unit.pixelDistanceCenter(a) < waypointDistancePixels && acceptableForSafety(a))) return mustApproach

    val waypointDistance = Math.max(waypointDistancePixels, if (requireSafety) 64 + unit.matchups.pixelsEntangled else 0)
    val rayRadians = With.reaction.sluggishness match {
      case 0 => rayRadians32
      case 1 => rayRadians32
      case 2 => rayRadians16
      case _ => rayRadians12
    }
    val terminus = rayRadians
      .view
      .map(r => castRay(unit.pixel, lengthPixels = waypointDistance, radians = radians + r, flying = unit.flying))
      .find(p => {
        val clamped = p.clamp(unit.unitClass.dimensionMax / 2)
        (unit.pixelDistanceCenter(clamped) >= 80
          && unacceptableDistance.forall(_ > clamped.travelPixelsFor(mustApproach.get, unit))
          && acceptableForSafety(unit.pixel.project(clamped, 80)))
      })
    terminus
  }

  def tryMovingAlongTilePath(unit: FriendlyUnitInfo, path: TilePath): Unit = {
    getWaypointAlongTilePath(unit, path)
      .map(_.add(unit.pixel.offsetFromTileCenter))
      .foreach(waypoint => {
        unit.agent.decision.set(waypoint)
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
    if (unit.agent.forces.allZero) {
      unit.agent.forces(Forces.travel)  = Potential.towards(unit, unit.agent.destinationNext())
    }
    unit.agent.forces(Forces.spacing)   = Potential.preferSpacing(unit)
    unit.agent.forces(Forces.pushing)   = Potential.followPushes(unit)
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
    Shapes
      .Ray(from, lengthPixels = lengthPixels, radians = radians)
      .foreach(pixel => {
        proceed &&= pixel.valid
        proceed &&= flying || pixel.walkableUnchecked
        if (proceed) output = pixel
      })
    output
  }

  def pullTowards(leash: Double, towards: Pixel*): Pixel = {
    var distanceLeft  = leash
    val from          = towards.headOption.getOrElse(Points.middle)
    var output        = from
    towards.drop(1).foreach(to => {
      if (distanceLeft > 0) {
        val distanceTo  = Math.min(distanceLeft, output.pixelDistance(to))
        output          = output.project(to, distanceTo)
        distanceLeft    -= distanceTo
      }
    })
    output
  }
}
