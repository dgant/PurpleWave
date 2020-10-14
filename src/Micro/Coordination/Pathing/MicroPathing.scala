package Micro.Coordination.Pathing

import Debugging.Visualizations.ForceColors
import Information.Geography.Pathfinding.Types.TilePath
import Information.Geography.Pathfinding.{PathfindProfile, PathfindRepulsor}
import Lifecycle.With
import Mathematics.Physics.{Force, ForceMath}
import Mathematics.Points.{Pixel, PixelRay}
import Mathematics.PurpleMath
import Micro.Actions.Commands.Move
import Micro.Coordination.Pushing.Push
import Micro.Heuristics.Potential
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.TakeN

object MicroPathing {

  def getRealPath(unit: FriendlyUnitInfo, desire: DesireProfile): MicroPath = {
    val pathLengthMinimum = 7

    val output                                    = new MicroPath(unit)
    output.desire                                 = Some(desire)
    output.pathfindProfile                        = Some(new PathfindProfile(unit.tileIncludingCenter))
    output.pathfindProfile.get.end                = if (output.desire.get.home > 0) Some(unit.agent.origin.tileIncluding) else None
    output.pathfindProfile.get.lengthMinimum      = Some(pathLengthMinimum)
    output.pathfindProfile.get.lengthMaximum      = Some(PurpleMath.clamp((unit.matchups.framesOfEntanglement * unit.topSpeed + unit.effectiveRangePixels).toInt / 32, pathLengthMinimum, 15))
    output.pathfindProfile.get.threatMaximum      = Some(0)
    output.pathfindProfile.get.canCrossUnwalkable = unit.flying || unit.transport.exists(_.flying)
    output.pathfindProfile.get.allowGroundDist    = true
    output.pathfindProfile.get.costOccupancy      = if (output.pathfindProfile.get.canCrossUnwalkable) 0f else 3f
    output.pathfindProfile.get.costThreat         = 6f
    output.pathfindProfile.get.costRepulsion      = 2.5f
    output.pathfindProfile.get.repulsors          = getPathfindingRepulsors(unit)
    output.pathfindProfile.get.unit               = Some(unit)
    output.tilePath                               = Some(output.pathfindProfile.get.find)
    output.to                                     = getWaypointAlongTilePath(output.tilePath.get)
    output
  }

  def getWaypointAlongTilePath(path: TilePath): Option[Pixel] = {
    // Moving 5 tiles ahead was the recommendation from McRave.
    // It avoids trying to move units immediately to the other side of a building,
    // which can cause them to get stuck against that building.
    if (path.pathExists) Some(path.tiles.get.take(5).last.pixelCenter) else None
  }

  def tryMovingAlongTilePath(unit: FriendlyUnitInfo, path: TilePath): Unit = {
    val waypoint = getWaypointAlongTilePath(path)
    waypoint.foreach(pixel => {
      unit.agent.toTravel = waypoint
      Move.delegate(unit)
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

  def setRetreatPotentials(unit: FriendlyUnitInfo, desire: DesireProfile): Unit = {
    unit.agent.toTravel = Some(unit.agent.origin)

    if (unit.isAny(Protoss.Carrier, Zerg.Guardian)) {
      val threats           = unit.matchups.threats
      val walkers           = threats.view.filter(threat => ! threat.flying && threat.zone == unit.zone)
      val dpfFromThreats    = threats.map(_.dpfOnNextHitAgainst(unit)).sum
      val dpfFromWalkers    = walkers.map(_.dpfOnNextHitAgainst(unit)).sum
      val cliffingMagnitude = PurpleMath.nanToZero(dpfFromWalkers / dpfFromThreats)
      val forceCliffing     = Potential.cliffAttraction(unit).normalize(0.5 * cliffingMagnitude)
      unit.agent.forces.put(ForceColors.sneaking, forceCliffing)
    }

    val bonusPreferExit   = if (unit.agent.origin.zone != unit.zone) 1.0 else if (unit.matchups.threats.exists(_.topSpeed < unit.topSpeed)) 0.0 else 0.5
    val bonusRegrouping   = 9.0 / Math.max(24.0, unit.matchups.framesOfEntanglement)

    unit.agent.forces.put(ForceColors.threat,     Potential.avoidThreats(unit)      * desire.safety)
    unit.agent.forces.put(ForceColors.traveling,  Potential.preferTravelling(unit)  * desire.home * bonusPreferExit)
    unit.agent.forces.put(ForceColors.spreading,  Potential.preferSpreading(unit)   * desire.safety * desire.freedom)
    unit.agent.forces.put(ForceColors.regrouping, Potential.preferRegrouping(unit)  * bonusRegrouping)
    unit.agent.forces.put(ForceColors.spacing,    Potential.avoidCollision(unit)    * desire.freedom)
    unit.agent.forces.put(ForceColors.sneaking,   Potential.detectionRepulsion(unit))
  }

  def getAvoidDirectForce(unit: FriendlyUnitInfo, desire: DesireProfile): Option[Force] = {
    if (desire.safety <= 0) return None

    val origin = unit.agent.origin
    if (desire.home > 0 && origin.zone != unit.zone && unit.matchups.threats.exists(threat => {
      val rangeThreat     = threat.pixelRangeAgainst(unit)
      val distanceThreat  = threat.pixelDistanceTravelling(origin)
      val distanceUs      = unit.pixelDistanceTravelling(origin)
      // Do they cut us off (by speed or pure range) before we get home?
      (distanceThreat - rangeThreat) / (Math.min(threat.topSpeed, unit.topSpeed)) < distanceUs / unit.topSpeed
    })) {
      return None
    }

    // Same as old, but with clipmin(1)
    val forceThreat   = (Potential.avoidThreats(unit)                     * desire.safety).clipMin(1.0)
    // Same as old, but old uses zone pathing and bonusPreferExit, while new uses ring + travel distance (though it should use straight directions for fliers
    val forceTravel   = (Potential.preferTravel(unit, unit.agent.origin)  * desire.home)
    // Same as old, but with clipmax(0.5)
    val forceSpacing  = (Potential.avoidCollision(unit) * desire.freedom).clipMax(0.5)
    // Missing: Regrouping
    // Missing: Spreading
    // New math
    val forceSafety   = ForceMath.sumAll(forceThreat, forceTravel)

    // If the safety and travel forces are in conflict, we need to resolve it intelligently
    if (forceSafety.lengthSquared > 0 && forceTravel.lengthSquared > 0) {
      val radianDifference = PurpleMath.radiansTo(forceThreat.radians, forceTravel.radians)
      if (radianDifference > Math.PI / 2) {
        return None
      }
    }

    Some(ForceMath.sumAll(forceSafety.normalize, forceSpacing))
  }

  def combinePushForces(pushForces: Seq[(Push, Force)]): Force = {
    if (pushForces.isEmpty) return new Force
    val highestPriority = pushForces.view.map(_._1.priority).max
    pushForces.filter(_._1.priority == highestPriority).map(_._2).reduce(_ + _)
  }

  def getPushForces(unit: FriendlyUnitInfo): Seq[(Push, Force)] = {
    With.coordinator.pushes.get(unit).map(p => (p, p.force(unit))).filter(_._2.exists(_.lengthSquared > 0)).map(p => (p._1, p._2.get))
  }

  def getPushForce(unit: FriendlyUnitInfo): Force = {
    combinePushForces(getPushForces(unit))
  }

  def applyDirectForce(unit: FriendlyUnitInfo, desire: DesireProfile, force: Force): Unit = {
    val pathLength = 64 + unit.matchups.pixelsOfEntanglement
    val stepSize = Math.min(pathLength, Math.max(64, unit.topSpeed * With.reaction.agencyMax))
    val origin = unit.agent.origin

    // Find a walkable path
    val rotationSteps = 4
    (1 to 1 + 2 * rotationSteps).foreach(r => {
      if (unit.ready) {
        val rotationRadians = ((r % 2) * 2 - 1) * (r / 2) * Math.PI / 3 / rotationSteps
        val ray = PixelRay(unit.pixelCenter, unit.pixelCenter.radiateRadians(force.radians + rotationRadians, pathLength))
        val destination = ray.to
        // Verify validity of the path
        if (destination.valid) {
          if (unit.flying || ray.tilesIntersected.forall(_.walkable)) {
            // Verify that it takes us home, if necessary
            if (unit.zone == origin.zone || desire.home * unit.pixelDistanceTravelling(destination, origin) <= desire.home * unit.pixelDistanceTravelling(origin)) {
              // Verify that it gets us away from threats
              // Compare distance to a tiny incremental step so it rejects walking through enemies
              if (unit.matchups.threats.forall(threat => threat.pixelDistanceSquared(unit.pixelCenter) < threat.pixelDistanceSquared(unit.pixelCenter.project(destination, 16)))) {
                ray.tilesIntersected.foreach(With.coordinator.gridPathOccupancy.addUnit(unit, _))
                unit.agent.toTravel = Some(unit.pixelCenter.project(destination, stepSize)) // Use smaller step size to use direct line travel instead of BW's 8-directional long distance moves
                Move.delegate(unit)
              }
            }
          }
        }
      }
    })
  }

  def setDestinationUsingAgentForces(unit: FriendlyUnitInfo) {
    def useShortAreaPathfinding(unit: FriendlyUnitInfo): Boolean = {
      ! unit.flying && ! unit.transport.exists(_.flying) && unit.agent.destination.zone == unit.zone
    }
    val rayDistance = 32.0 * 3.0
    val cardinal8directions = (0.0 until 2.0 by 0.25).map(_ * Math.PI).toVector
    val framesAhead = With.reaction.agencyAverage + 1
    val minDistance = Math.max(48, unit.unitClass.haltPixels + framesAhead * (unit.topSpeed + 0.5 * framesAhead * unit.topSpeed / Math.max(1, unit.unitClass.accelerationFrames)))
    val forces      = unit.agent.forces.values
    val origin      = unit.pixelCenter
    val forceSum    = ForceMath.sum(forces).normalize
    val rayLength   = Math.max(rayDistance, minDistance)
    val pathfind    = useShortAreaPathfinding(unit)
    def makeRay(radians: Double): PixelRay = {
      PixelRay(unit.pixelCenter, unit.pixelCenter.radiateRadians(radians, rayLength))
    }

    lazy val forceRadians = forceSum.radians
    lazy val ray          = makeRay(forceRadians)
    lazy val rayWalkable  = ray.tilesIntersected.forall(With.grids.walkable.get)

    if ( ! pathfind || rayWalkable) {
      var destination = unit.pixelCenter.add(forceSum.normalize(rayLength).toPoint)
      // Prevent unit from getting confused from trying to move too close to unwalkable tiles
      if ( ! unit.unitClass.corners.map(destination.add(_).tileIncluding).forall(unit.canTraverse)) {
        destination = destination.tileIncluding.pixelCenter
      }
      unit.agent.toTravel = Some(destination)

      return
    }

    val angles = cardinal8directions.filter(r => Math.abs(PurpleMath.radiansTo(r, forceRadians)) <= Math.PI * 0.75)
    val paths = angles.map(makeRay) :+ ray
    val pathsTruncated = paths.map(ray =>
      PixelRay(
        ray.from,
        ray.from.project(
          ray.to,
          ray
            .tilesIntersected
            .takeWhile(tile => tile.valid && With.grids.walkable.get(tile))
            .lastOption
            .map(_.pixelCenter.pixelDistance(ray.from))
            .getOrElse(0.0))))

    val pathAccepted = pathsTruncated.maxBy(ray => ray.length * (1.0 + Math.cos(ray.radians - forceRadians)))
    val commandPixel = unit.pixelCenter.project(pathAccepted.to, minDistance)
    unit.agent.pathsAll = paths
    unit.agent.pathsTruncated = pathsTruncated
    unit.agent.pathsAcceptable = Vector(pathAccepted)
    unit.agent.toTravel = Some(commandPixel)

    if (!unit.flying) {
      unit.zone.edges.find(e => unit.pixelDistanceCenter(e.pixelCenter) < e.radiusPixels).foreach(edge => {
        unit.agent.toTravel = Some(
          unit.pixelCenter.project(
            edge.sidePixels.minBy(ep => PurpleMath.radiansTo(
              unit.pixelCenter.radiansTo(commandPixel),
              edge.pixelCenter.radiansTo(ep))),
          128))
      })
    }
  }


}
