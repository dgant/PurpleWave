package Micro.Coordination.Pathing

import Debugging.Visualizations.ForceColors
import Information.Geography.Pathfinding.Types.TilePath
import Information.Geography.Pathfinding.{PathfindProfile, PathfindRepulsor}
import Lifecycle.With
import Mathematics.Physics.{Force, ForceMath}
import Mathematics.Points.PixelRay
import Mathematics.PurpleMath
import Micro.Actions.Commands.Move
import Micro.Coordination.Pushing.Push
import Micro.Heuristics.Potential
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.TakeN

object MicroPathing {

  def getRealPath(unit: FriendlyUnitInfo, desire: DesireProfile): TilePath = {
    val pathLengthMinimum = 7
    val maximumDistance = PurpleMath.clamp((unit.matchups.framesOfEntanglement * unit.topSpeed + unit.effectiveRangePixels).toInt / 32, pathLengthMinimum, 15)

    val profile = new PathfindProfile(unit.tileIncludingCenter)
    profile.end                 = if (desire.home > 0) Some(unit.agent.origin.tileIncluding) else None
    profile.lengthMinimum       = Some(pathLengthMinimum)
    profile.lengthMaximum       = Some(maximumDistance)
    profile.threatMaximum       = Some(0)
    profile.canCrossUnwalkable  = unit.flying || unit.transport.exists(_.flying)
    profile.allowGroundDist     = true
    profile.costOccupancy       = if (profile.canCrossUnwalkable) 0f else 3f
    profile.costThreat          = 6f
    profile.costRepulsion       = 2.5f
    profile.repulsors           = getPathfindingRepulsors(unit)
    profile.unit = Some(unit)
    profile.find
  }


  def getPathfindingRepulsors(unit: FriendlyUnitInfo, maxThreats: Int = 10): IndexedSeq[PathfindRepulsor] = {
    TakeN
      .by(maxThreats, unit.matchups.threats.view.filter(_.likelyStillThere))(Ordering.by(t => unit.matchups.framesOfEntanglementPerThreat(t)))
      .map(t => PathfindRepulsor(
        t.pixelCenter,
        t.dpfOnNextHitAgainst(unit),
        64 + t.pixelRangeAgainst(unit)))
      .toIndexedSeq
  }

  def setRetreatPotentials(unit: FriendlyUnitInfo, desire: DesireProfile): Unit = {
    unit.agent.toTravel = Some(unit.agent.origin)

    val bonusAvoidThreats = PurpleMath.clamp(With.reaction.agencyAverage + unit.matchups.framesOfEntanglement, 12.0, 24.0) / 12.0
    val bonusPreferExit   = if (unit.agent.origin.zone != unit.zone) 1.0 else if (unit.matchups.threats.exists(_.topSpeed < unit.topSpeed)) 0.0 else 0.5
    val bonusRegrouping   = 9.0 / Math.max(24.0, unit.matchups.framesOfEntanglement)

    val forceThreat         = Potential.avoidThreats(unit)      * desire.safety
    val forceSpacing        = Potential.avoidCollision(unit)    * desire.freedom
    val forceExiting        = Potential.preferTravelling(unit)  * desire.home * bonusPreferExit
    val forceSpreading      = Potential.preferSpreading(unit)   * desire.safety * desire.freedom
    val forceRegrouping     = Potential.preferRegrouping(unit)  * bonusRegrouping
    val forceSneaking       = Potential.detectionRepulsion(unit)

    if (unit.isAny(Protoss.Carrier, Zerg.Guardian)) {
      val threats           = unit.matchups.threats
      val walkers           = threats.filter(threat => ! threat.flying && threat.zone == unit.zone)
      val dpfFromWalkers    = walkers.map(_.dpfOnNextHitAgainst(unit)).sum
      val dpfFromThreats    = threats.map(_.dpfOnNextHitAgainst(unit)).sum
      val cliffingMagnitude = PurpleMath.nanToZero(dpfFromWalkers / dpfFromThreats)
      val forceCliffing     = Potential.cliffAttraction(unit).normalize(0.5 * cliffingMagnitude)
      unit.agent.forces.put(ForceColors.sneaking, forceCliffing)
    }

    unit.agent.forces.put(ForceColors.threat,         forceThreat)
    unit.agent.forces.put(ForceColors.traveling,      forceExiting)
    unit.agent.forces.put(ForceColors.spreading,      forceSpreading)
    unit.agent.forces.put(ForceColors.regrouping,     forceRegrouping)
    unit.agent.forces.put(ForceColors.spacing,        forceSpacing)
    unit.agent.forces.put(ForceColors.sneaking,       forceSneaking)
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

    val forceThreat   = (Potential.avoidThreats(unit)                     * desire.safety).clipMin(1.0)
    val forceTravel   = (Potential.preferTravel(unit, unit.agent.origin)  * desire.home)
    val forceSpacing  = (Potential.avoidCollision(unit) * desire.freedom).clipMax(0.5)
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

  def getPushForces(unit: FriendlyUnitInfo): Seq[(Push, Force)] = {
    With.coordinator.pushes.get(unit).map(p => (p, p.force(unit))).filter(_._2.exists(_.lengthSquared > 0)).map(p => (p._1, p._2.get))
  }

  def getPushForce(unit: FriendlyUnitInfo, minPriority: Int = -1): Option[Force] = {
    val pushedForces = getPushForces(unit).filter(_._1.priority >= minPriority)
    if (pushedForces.isEmpty) return None
    val forces = pushedForces.map(p => p._2 * Math.pow(2, p._1.priority))
    val output = ForceMath.sum(forces).normalize
    Some(output)
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
}
