package Micro.Actions.Combat.Maneuvering

import Debugging.Visualizations.ForceColors
import Information.Geography.Pathfinding.{PathfindProfile, PathfindRepulsor}
import Lifecycle.With
import Mathematics.Physics.ForceMath
import Mathematics.Points.PixelRay
import Mathematics.PurpleMath
import Micro.Actions.Action
import Micro.Actions.Combat.Tactics.Potshot
import Micro.Actions.Commands.{Gravitate, Move}
import Micro.Heuristics.Potential
import Planning.UnitMatchers.{UnitMatchSiegeTank, UnitMatchWorkers}
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.{ByOption, TakeN}

object Avoid extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.canMove && unit.matchups.threats.nonEmpty

  case class DesireProfile(home: Int, safety: Int, freedom: Int, target: Int = 0) {
    def distance(other: DesireProfile): Double = Seq(
      Math.pow(home - other.home, 2),
      Math.pow(safety - other.safety, 2),
      Math.pow(freedom - other.freedom, 2),
      Math.pow(target - other.target, 2),
    ).sum
  }

  def pathfindingRepulsion(unit: FriendlyUnitInfo, maxThreats: Int = 10): IndexedSeq[PathfindRepulsor] = {
    TakeN
      .by(maxThreats, unit.matchups.threats.view.filter(_.likelyStillThere))(Ordering.by(t => unit.matchups.framesOfEntanglementPerThreat(t)))
      .map(t => PathfindRepulsor(
        t.pixelCenter,
        t.dpfOnNextHitAgainst(unit),
        64 + t.pixelRangeAgainst(unit)))
      .toIndexedSeq
  }

  override def perform(unit: FriendlyUnitInfo): Unit = {
    def timeOriginOfThreat(threat: UnitInfo): Double = threat.framesToTravelTo(unit.agent.origin) - threat.pixelRangeAgainst(unit) * threat.topSpeed

    val distanceOriginUs    = unit.pixelDistanceTravelling(unit.agent.origin)
    val distanceOriginEnemy = ByOption.min(unit.matchups.threats.view.map(t => t.pixelDistanceTravelling(unit.agent.origin) - t.pixelRangeAgainst(unit))).getOrElse(2.0 * With.mapPixelWidth)
    val enemyCloser         = distanceOriginUs + 160 >= distanceOriginEnemy
    val timeOriginUs        = unit.framesToTravelTo(unit.agent.origin)
    val timeOriginEnemy     = TakeN.percentile(0.1, unit.matchups.threats)(Ordering.by(timeOriginOfThreat)).map(timeOriginOfThreat).getOrElse(Double.PositiveInfinity)
    val enemySooner         = timeOriginUs + 96 >= timeOriginEnemy
    val enemySieging        = unit.matchups.enemies.exists(_.isAny(UnitMatchSiegeTank, Zerg.Lurker)) && ! unit.base.exists(_.owner.isEnemy)
    val atHome              = unit.zone == unit.agent.origin.zone
    val scouting            = unit.agent.canScout
    val desireToGoHome      =
      if (unit.is(Protoss.DarkTemplar))
        -1
      else if (enemySieging && ! enemyCloser && ! enemySooner)
        -1
      else if (scouting || atHome)
        0
      else if (unit.base.exists(_.owner.isEnemy))
        2
      else
        ((if (enemyCloser) 1 else 0) + (if (enemySooner) 1 else 0))

    val desireForFreedom  = if (unit.flying && ! unit.matchups.threats.exists(_.unitClass.dealsRadialSplashDamage)) 0 else 1
    val desireForSafety   = PurpleMath.clamp(0, 3, (3 * (1 - unit.matchups.framesOfSafety / 72)).toInt)
    val desireProfile     = DesireProfile(desireToGoHome, desireForSafety, desireForFreedom)

    // Don't spray Zealots out against melee units, especially Zerglings
    if (unit.is(Protoss.Zealot)
      && unit.base == unit.agent.origin.base
      && unit.agent.origin.base.exists(_.isOurMain)
      && unit.matchups.threats.exists( ! _.unitClass.isWorker)
      && unit.matchups.threats.forall(_.isAny(Protoss.Zealot, Zerg.Zergling, UnitMatchWorkers))) {
      unit.agent.toTravel = unit.agent.origin.base.map(_.heart.pixelCenter)

      // Poke back at enemies -- likely Zerglings -- while retreating
      if ( ! unit.matchups.threats.exists(_.is(Protoss.Zealot))) {
        Potshot.delegate(unit)
      }
      Move.delegate(unit)
      return
    }

    // If traveling by air, potential provides the smoothest paths
    if (unit.flying || (unit.transport.exists(_.flying) && unit.matchups.framesOfSafety <= 0)) {
      avoidPotential(unit, desireProfile)
      return
    }

    // Try the smoothest, fastest solution: Direct potential
    avoidDirect(unit, desireProfile)

    // Apply threat-aware pathfinding to try finding a better solution
    if ( ! With.performance.danger) {
      avoidRealPath(unit, desireProfile)
    }

    // If home is safe, run directly there, possibly through enemy fire
    val originBase = unit.agent.origin.base
    if (originBase.isDefined && unit.base != originBase && ! unit.matchups.threats.exists(_.base == originBase)) {
      unit.agent.toTravel = Some(unit.agent.origin)
      Move.delegate(unit)
    }

    // Last resort: Potential fields, which by now are probably totally out-of-tune for ground units
    if (desireToGoHome >= 0) {
      unit.agent.toTravel = Some(unit.agent.origin)
    } else {
      avoidPotential(unit, desireProfile)
    }
  }

  def avoidDirect(unit: FriendlyUnitInfo, desireProfile: DesireProfile): Unit = {
    if ( ! unit.ready) return

    if (desireProfile.safety <= 0) return

    val origin = unit.agent.origin
    if (desireProfile.home > 0 && origin.zone != unit.zone && unit.matchups.threats.exists(threat => {
      val rangeThreat     = threat.pixelRangeAgainst(unit)
      val distanceThreat  = threat.pixelDistanceTravelling(origin)
      val distanceUs      = unit.pixelDistanceTravelling(origin)
      // Do they cut us off (by speed or pure range) before we get home?
      (distanceThreat - rangeThreat) / (Math.min(threat.topSpeed, unit.topSpeed)) < distanceUs / unit.topSpeed
    })) {
      return
    }

    val pathLength    = 64 + unit.matchups.pixelsOfEntanglement
    val stepSize      = Math.min(pathLength, Math.max(64, unit.topSpeed * With.reaction.agencyMax))
    val forceThreat   = (Potential.avoidThreats(unit)                     * desireProfile.safety).clipMin(1.0)
    val forceTravel   = (Potential.preferTravel(unit, unit.agent.origin)  * desireProfile.home)
    val forceSpacing  = (Potential.avoidCollision(unit) * desireProfile.freedom).clipMax(0.5)
    val forceSafety   = ForceMath.sumAll(forceThreat, forceTravel)
    val force         = ForceMath.sumAll(forceSafety.normalize, forceSpacing)

    // If the safety and travel forces are in conflict, we need to resolve it intelligently
    if (forceSafety.lengthSquared > 0 && forceTravel.lengthSquared > 0) {
      val radianDifference = PurpleMath.radiansTo(forceThreat.radians, forceTravel.radians)
      if (radianDifference > Math.PI / 2) {
        return
      }
    }

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
            if (unit.zone == origin.zone || desireProfile.home * unit.pixelDistanceTravelling(destination, origin) <= desireProfile.home * unit.pixelDistanceTravelling(origin)) {
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

  def avoidRealPath(unit: FriendlyUnitInfo, desireProfile: DesireProfile): Unit = {
    if (! unit.ready) return

    val pathLengthMinimum = 7
    val maximumDistance = PurpleMath.clamp((unit.matchups.framesOfEntanglement * unit.topSpeed + unit.effectiveRangePixels).toInt / 32, pathLengthMinimum, 15)

    val profile = new PathfindProfile(unit.tileIncludingCenter)
    profile.end                 = if (desireProfile.home > 0) Some(unit.agent.origin.tileIncluding) else None
    profile.lengthMinimum       = Some(pathLengthMinimum)
    profile.lengthMaximum       = Some(maximumDistance)
    profile.threatMaximum       = Some(0)
    profile.canCrossUnwalkable  = unit.flying || unit.transport.exists(_.flying)
    profile.allowGroundDist     = true
    profile.costOccupancy       = if (profile.canCrossUnwalkable) 0f else 3f
    profile.costThreat          = 6f
    profile.costRepulsion       = 2.5f
    profile.repulsors           = pathfindingRepulsion(unit)
    profile.unit = Some(unit)
    val path = profile.find

    new Traverse(path).delegate(unit)
  }

  def avoidPotential(unit: FriendlyUnitInfo, desireProfile: DesireProfile): Unit = {

    if ( ! unit.ready) return

    unit.agent.toTravel = Some(unit.agent.origin)

    val bonusAvoidThreats = PurpleMath.clamp(With.reaction.agencyAverage + unit.matchups.framesOfEntanglement, 12.0, 24.0) / 12.0
    val bonusPreferExit   = if (unit.agent.origin.zone != unit.zone) 1.0 else if (unit.matchups.threats.exists(_.topSpeed < unit.topSpeed)) 0.0 else 0.5
    val bonusRegrouping   = 9.0 / Math.max(24.0, unit.matchups.framesOfEntanglement)

    val forceThreat         = Potential.avoidThreats(unit)      * desireProfile.safety
    val forceSpacing        = Potential.avoidCollision(unit)    * desireProfile.freedom
    val forceExiting        = Potential.preferTravelling(unit)  * desireProfile.home * bonusPreferExit
    val forceSpreading      = Potential.preferSpreading(unit)   * desireProfile.safety * desireProfile.freedom
    val forceRegrouping     = Potential.preferRegrouping(unit)  * bonusRegrouping
    val forceMobility       = Potential.preferMobility(unit)
    val forceSneaking       = Potential.detectionRepulsion(unit)
    val resistancesTerrain  = Potential.resistTerrain(unit)
    
    unit.agent.forces.put(ForceColors.threat,         forceThreat)
    unit.agent.forces.put(ForceColors.traveling,      forceExiting)
    unit.agent.forces.put(ForceColors.spreading,      forceSpreading)
    unit.agent.forces.put(ForceColors.regrouping,     forceRegrouping)
    unit.agent.forces.put(ForceColors.spacing,        forceSpacing)
    unit.agent.forces.put(ForceColors.mobility,       forceMobility)
    unit.agent.forces.put(ForceColors.sneaking,       forceSneaking)
    unit.agent.resistances.put(ForceColors.mobility,  resistancesTerrain)

    Gravitate.delegate(unit)
    Move.delegate(unit)
  }
}
