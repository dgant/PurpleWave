package Micro.Coordination.Pathing

import Debugging.Visualizations.Forces
import Information.Geography.Pathfinding.Types.TilePath
import Information.Geography.Pathfinding.{PathfindProfile, PathfindRepulsor}
import Lifecycle.With
import Mathematics.Physics.{Force, ForceMath}
import Mathematics.Points.{Pixel, PixelRay}
import Mathematics.PurpleMath
import Mathematics.Shapes.{Circle, Ring}
import Micro.Actions.Combat.Maneuvering.Retreat.DesireProfile
import Micro.Coordination.Pushing.Push
import Micro.Heuristics.Potential
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.{ByOption, TakeN}

import scala.collection.SeqView

object MicroPathing {

  def getRealPath(unit: FriendlyUnitInfo, preferHome: Boolean = true): TilePath = {
    val pathLengthMinimum = 7
    val pathfindProfile                 = new PathfindProfile(unit.tileIncludingCenter)
    pathfindProfile.end                 = if (preferHome) Some(unit.agent.origin.tileIncluding) else None
    pathfindProfile.lengthMinimum       = Some(pathLengthMinimum)
    pathfindProfile.lengthMaximum       = Some(PurpleMath.clamp((unit.matchups.framesOfEntanglement * unit.topSpeed + unit.effectiveRangePixels).toInt / 32, pathLengthMinimum, 15))
    pathfindProfile.threatMaximum       = Some(0)
    pathfindProfile.canCrossUnwalkable  = unit.flying || unit.transport.exists(_.flying)
    pathfindProfile.allowGroundDist     = true
    pathfindProfile.costOccupancy       = if (pathfindProfile.canCrossUnwalkable) 0f else 3f
    pathfindProfile.costThreat          = 6f
    pathfindProfile.costRepulsion       = 2.5f
    pathfindProfile.repulsors           = getPathfindingRepulsors(unit)
    pathfindProfile.unit                = Some(unit)
    pathfindProfile.find
  }

  // McRave recommends moving 5 tiles at a time when following path waypoints.
  // That distance avoids trying to move units immediately to the other side of a building,
  // which can cause them to get stuck against that building.
  val waypointDistanceTiles: Int = 5
  val waypointDistancePixels: Int = 32 * waypointDistanceTiles
  private val ringDistance = waypointDistanceTiles * waypointDistanceTiles * 32 * 32
  def getRingTowards(from: Pixel, to: Pixel): SeqView[Pixel, Seq[_]] = {
    (if (from.pixelDistanceSquared(to) >= ringDistance) Ring.points(5) else Circle.points(5)).view.map(p => from.add(p.x * 32, p.y * 32))
  }

  def getWaypointAlongTerrain(unit: UnitInfo, to: Pixel): Pixel = {
    if (unit.flying || unit.pixelDistanceTravelling(to) <= waypointDistancePixels || unit.zone == to.zone) {
      to
    } else {
      ByOption.minBy(getRingTowards(unit.pixelCenter, to).filter(_.tileIncluding.walkable))(_.groundPixels(to)).getOrElse(to)
    }
  }

  def getWaypointAlongTilePath(path: TilePath): Option[Pixel] = {
    if (path.pathExists) Some(path.tiles.get.take(waypointDistanceTiles).last.pixelCenter) else None
  }

  def tryMovingAlongTilePath(unit: FriendlyUnitInfo, path: TilePath): Unit = {
    val waypoint = getWaypointAlongTilePath(path)
    waypoint.foreach(pixel => {
      unit.agent.lastPath = Some(path)
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

  def setDefaultForces(unit: FriendlyUnitInfo, desire: DesireProfile = DesireProfile()): Unit = {
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
    unit.agent.forces(Forces.threat)      = (Potential.avoidThreats(unit)     * desire.safety)
    unit.agent.forces(Forces.traveling)   = (Potential.preferTravel(unit, to) * desire.home)
    unit.agent.forces(Forces.sneaking)    = (Potential.detectionRepulsion(unit))

    // How to get there
    unit.agent.forces(Forces.spreading)   = (Potential.preferSpreading(unit)  * desire.safety)
    unit.agent.forces(Forces.regrouping)  = (Potential.preferRegrouping(unit) * Math.max(0, 1 - desire.safety))
    unit.agent.forces(Forces.spacing)     = (Potential.avoidCollision(unit))

    ForceMath.rebalance(unit.agent.forces, 1.5, Forces.threat, Forces.traveling, Forces.sneaking)
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

  def getPushRadians(unit: FriendlyUnitInfo): Option[Double] = {
    getPushRadians(getPushForces(unit))
  }

  def qualifyRetreatDirection(
    unit: FriendlyUnitInfo,
    radians: Double,
    avoidThreats: Boolean = true,
    towards: Option[Pixel] = None): Option[Pixel] = {
    val pathLength = Math.max(waypointDistancePixels, 64 + unit.matchups.pixelsOfEntanglement)
    val ray = new PixelRay(unit.pixelCenter, pathLength, radians)
    // Is the endpoint valid?
    if ( ! ray.to.valid) return None
    // Is the path traversable?
    if ( ! unit.flying && ! ray.tilesIntersected.forall(_.walkable)) return None
    // Does it takes us towards our goal?
    if (towards.exists(t => unit.zone != t.zone && unit.pixelDistanceTravelling(t) <= unit.pixelDistanceTravelling(ray.to, t))) return None
    // Does it keep us safe?
    if (avoidThreats && unit.matchups.threats.exists(t => t.pixelDistanceSquared(ray.to) <= t.pixelDistanceSquared(unit.pixelCenter))) return None
    Some(ray.to)
  }

  private val rays = 16
  private val rayRadians = (0 to rays).map(_ * Math.PI / rays - Math.PI / 2).toVector.sortBy(Math.abs)
  def findRayTowards(unit: FriendlyUnitInfo, radians: Double, desire: DesireProfile = DesireProfile()): Option[Pixel] = {
    val pathLength = 64 + unit.matchups.pixelsOfEntanglement
    val stepSize = Math.min(pathLength, Math.max(64, unit.topSpeed * With.reaction.agencyMax))
    val origin = unit.agent.origin
    val output: Option[Pixel] = rayRadians.view.map(r =>
      qualifyRetreatDirection(
        unit,
        radians + r,
        desire.safety > 0,
        Some(unit.agent.origin).filter(unused => desire.home > 0)))
      .filter(_.isDefined)
      .take(1)
      .headOption
      .flatten
    output
  }
}
