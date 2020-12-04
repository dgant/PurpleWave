package Micro.Coordination.Pathing

import Debugging.Visualizations.Forces
import Information.Geography.Pathfinding.Types.TilePath
import Information.Geography.Pathfinding.{PathfindProfile, PathfindRepulsor}
import Lifecycle.With
import Mathematics.Physics.{Force, ForceMath}
import Mathematics.Points.{Pixel, PixelRay}
import Mathematics.PurpleMath
import Mathematics.Shapes.Circle
import Micro.Coordination.Pushing.Push
import Micro.Heuristics.Potential
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.{ByOption, TakeN}

import scala.collection.SeqView
import scala.util.control.Breaks

object MicroPathing {

  def getRealPath(unit: FriendlyUnitInfo, preferHome: Boolean = true): TilePath = {
    val pathLengthMinimum = 7
    val pathfindProfile                 = new PathfindProfile(unit.tileIncludingCenter)
    pathfindProfile.end                 = if (preferHome) Some(unit.agent.origin.tileIncluding) else None
    pathfindProfile.lengthMinimum       = Some(pathLengthMinimum)
    pathfindProfile.lengthMaximum       = Some(PurpleMath.clamp((unit.matchups.framesOfEntanglement * unit.topSpeed + unit.effectiveRangePixels).toInt / 32, pathLengthMinimum, 15))
    pathfindProfile.threatMaximum       = Some(0)
    pathfindProfile.employGroundDist     = true
    pathfindProfile.costOccupancy       = if (unit.flying) 0f else 3f
    pathfindProfile.costThreat          = 6f
    pathfindProfile.costRepulsion       = 2.5f
    pathfindProfile.repulsors           = getPathfindingRepulsors(unit)
    pathfindProfile.unit                = Some(unit)
    pathfindProfile.find
  }

  // Moving the wrong sorts of lengths can cause the unit to get stuck on obstacles.
  // For example, this is at 5 tiles: https://cdn.discordapp.com/attachments/421808419482370059/774666718793564220/unknown.png
  val waypointDistanceTiles: Int = 4
  val waypointDistancePixels: Int = 32 * waypointDistanceTiles
  private val ringDistance = waypointDistanceTiles * waypointDistanceTiles * 32 * 32
  def getCircleTowards(from: Pixel, to: Pixel): SeqView[Pixel, Seq[_]] = {
    Circle.points(5).view.map(p => from.add(p.x * 32, p.y * 32))
  }

  def getWaypointToPixel(unit: UnitInfo, goal: Pixel): Pixel = {
    if (unit.flying) return goal
    val line         = PixelRay(unit.pixelCenter, goal)
    val lineWaypoint = if (line.tilesIntersected.forall(_.walkable)) Some(unit.pixelCenter.project(goal, Math.min(unit.pixelDistanceCenter(goal), waypointDistancePixels))) else None
    val path         = With.paths.zonePath(unit.pixelCenter.zone, goal.zone)
    val pathWaypoint = path.flatMap(_.steps.find(step => step.from != unit.zone || unit.pixelDistanceCenter(step.edge.pixelCenter) > step.edge.radiusPixels)).map(_.edge.pixelCenter)
    val goalWaypoint = pathWaypoint.getOrElse(goal)
    val ring         = MicroPathing.getCircleTowards(unit.pixelCenter, goalWaypoint)
    val ringFiltered = ring.filter(t => PixelRay(unit.pixelCenter, t).tilesIntersected.view.drop(1).forall(_.walkable))
    val ringWaypoint = ByOption.minBy(ringFiltered)(goal.groundPixels)
    lineWaypoint.orElse(ringWaypoint).orElse(pathWaypoint).getOrElse(goal)
  }

  def getWaypointAlongTilePath(path: TilePath): Option[Pixel] = {
    if (path.pathExists) Some(path.tiles.get.take(waypointDistanceTiles).last.pixelCenter) else None
  }

  private val rays = 8 //256
  private val rayRadians = (0 to rays).map(_ * 2 * Math.PI / rays - Math.PI).toVector.sortBy(Math.abs)
  private val rayCosines = rayRadians.map(Math.cos)
  private val terminusPixelsMinimum = waypointDistancePixels * 3 / 4
  def getWaypointInDirection(unit: FriendlyUnitInfo, radians: Double, mustApproach: Option[Pixel] = None, requireSafety: Boolean = false): Option[Pixel] = {
    lazy val safetyPixels =  unit.matchups.pixelsOfEntanglement
    lazy val travelDistanceCurrent = mustApproach.map(unit.pixelDistanceTravelling)

    def acceptableForSafety(pixel: Pixel): Boolean = {
      ! requireSafety || unit.matchups.threats.forall(t => t.pixelDistanceSquared(pixel) > t.pixelDistanceSquared(unit.pixelCenter))
    }

    if (mustApproach.exists(a => unit.pixelDistanceCenter(a) < waypointDistancePixels && acceptableForSafety(a))) {
      return mustApproach
    }

    val rayStart = unit.pixelCenter
    val waypointDistance = Math.max(waypointDistancePixels, if (requireSafety) 64 + safetyPixels else 0)
    val termini = rayRadians
      .indices
      .view
      .map(i => {
        val deltaRadians = rayRadians(i)
        val cosine = rayCosines(i)
        val terminus = castRay(rayStart, lengthPixels = waypointDistance, radians = radians + deltaRadians, flying = unit.flying)
        (terminus, cosine)
      })
      .filter(p => {
        val terminus = p._1.clamp(unit.unitClass.dimensionMax / 2)
        val cosine = p._2
        lazy val towardsTerminus = unit.pixelCenter.project(terminus, 8)
        lazy val travelDistanceTerminus = mustApproach.map(a => if (unit.flying) terminus.pixelDistance(a) else terminus.groundPixels(a))
        (
          terminus.pixelDistance(rayStart) > terminusPixelsMinimum
          && (travelDistanceTerminus.forall(_ < travelDistanceCurrent.get))
          && (acceptableForSafety(towardsTerminus)))
      })
    ByOption.maxBy(termini)(p => rayStart.pixelDistance(p._1) * p._2).map(_._1) // Return the terminus that moves us furthest along the desired axis
  }

  def tryMovingAlongTilePath(unit: FriendlyUnitInfo, path: TilePath): Unit = {
    val waypoint = getWaypointAlongTilePath(path)
    waypoint.foreach(pixel => {
      unit.agent.toTravel = waypoint
      With.commander.move(unit)
    })
  }

  def getPathfindingRepulsors(unit: FriendlyUnitInfo, maxThreats: Int = 10): IndexedSeq[PathfindRepulsor] = {
    TakeN
      .by(maxThreats, unit.matchups.threats.view.filter(_.likelyStillThere))(Ordering.by(t => unit.matchups.framesOfEntanglementPerThreat(t)))
      .map(threat => PathfindRepulsor(
        threat.pixelCenter,
        threat.dpfOnNextHitAgainst(unit),
        64 + threat.pixelRangeAgainst(unit)))
      .toIndexedSeq
  }

  def setDefaultForces(unit: FriendlyUnitInfo, goalSafety: Boolean, goalHome: Boolean): Unit = {
    val to = unit.agent.origin

    // Add cliffing
    if (unit.isAny(Protoss.Carrier, Zerg.Guardian)) {
      val threats           = unit.matchups.threats
      val walkers           = threats.view.filter(threat => ! threat.flying && threat.zone == unit.zone)
      val dpfFromThreats    = threats.map(_.dpfOnNextHitAgainst(unit)).sum
      val dpfFromWalkers    = walkers.map(_.dpfOnNextHitAgainst(unit)).sum
      val cliffingMagnitude = PurpleMath.nanToZero(dpfFromWalkers / dpfFromThreats)
      val forceCliffing     = Potential.cliffAttraction(unit).normalize(0.5 * cliffingMagnitude)
      unit.agent.forces.put(Forces.sneaking, forceCliffing)
    }

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
    new PixelRay(from, lengthPixels = lengthPixels, radians = radians)
      .tilesIntersected
      .foreach(tile => {
        if (tile.valid && (flying || tile.walkableUnchecked)) {
          output = tile.pixelCenter
        } else Breaks.break
      })
    output
  }
}
