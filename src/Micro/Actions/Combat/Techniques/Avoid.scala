package Micro.Actions.Combat.Techniques

import Debugging.Visualizations.ForceColors
import Information.Geography.Pathfinding.{PathfindProfile, PathfindRepulsor}
import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Actions.Combat.Maneuvering.DownhillPathfinder
import Micro.Actions.Combat.Techniques.Common.ActionTechnique
import Micro.Actions.Commands.{Gravitate, Move}
import Micro.Decisions.Potential
import Planning.UnitMatchers.UnitMatchSiegeTank
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.{ByOption, TakeN}

object Avoid extends ActionTechnique {

  // Run away!
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.canMove && (unit.matchups.threats.nonEmpty || (unit.effectivelyCloaked && unit.matchups.enemyDetectors.nonEmpty))
  
  override val applicabilityBase: Double = 0.5
  
  override def applicabilitySelf(unit: FriendlyUnitInfo): Double = {
    val meleeFactor   = if (unit.unitClass.ranged) 1.0 else 0.7
    val visionFactor  = if (unit.visibleToOpponents) 1.0 else 0.5
    val safetyFactor  = PurpleMath.clampToOne((36.0 + unit.matchups.framesOfEntanglement) / 24.0)
    val output        = meleeFactor * visionFactor * safetyFactor
    output
  }
  
  override def applicabilityOther(unit: FriendlyUnitInfo, other: UnitInfo): Option[Double] = {
    if (other.isFriendly) return None
    if ( ! other.canAttack(unit)) return None
    Some(1.0)
  }

  case class DesireProfile(home: Int, safety: Int, freedom: Int, target: Int = 0) {
    def distance(other: DesireProfile): Double = Seq(
      Math.pow(home - other.home, 2),
      Math.pow(safety - other.safety, 2),
      Math.pow(freedom - other.freedom, 2),
      Math.pow(target - other.target, 2),
    ).sum
  }

  override def perform(unit: FriendlyUnitInfo): Unit = {
    def timeOriginOfThreat(threat: UnitInfo): Double = threat.framesToTravelTo(unit.agent.origin) - threat.pixelRangeAgainst(unit)

    val distanceOriginUs    = unit.pixelDistanceTravelling(unit.agent.origin)
    val distanceOriginEnemy = ByOption.min(unit.matchups.threats.view.map(t => t.pixelDistanceTravelling(unit.agent.origin) + t.pixelRangeAgainst(unit))).getOrElse(2.0 * With.mapPixelWidth)
    val enemyCloser         = distanceOriginUs + 128 >= distanceOriginEnemy
    val timeOriginUs        = unit.framesToTravelTo(unit.agent.origin)
    val timeOriginEnemy     = TakeN.percentile(0.1, unit.matchups.threats)(Ordering.by(timeOriginOfThreat)).map(timeOriginOfThreat).getOrElse(Double.PositiveInfinity)
    val enemySooner         = timeOriginUs + 96 >= timeOriginEnemy
    val enemySieging        = unit.matchups.enemies.exists(_.isAny(UnitMatchSiegeTank, Zerg.Lurker))
    val atHome              = unit.zone == unit.agent.origin.zone
    val scouting            = unit.agent.canScout
    val desireToGoHome      =
      if ( ! With.configuration.retreatTowardsHomeOptional)
        1
      else if (enemySieging)
        -1
      else if (scouting || atHome)
        0
      else
        ((if (enemyCloser) 1 else 0) + (if (enemySooner) 1 else 0))

    val desireForFreedom    = if (unit.flying && ! unit.matchups.threats.exists(_.unitClass.dealsRadialSplashDamage)) 0 else 1
    val desireForSafety     = PurpleMath.clamp(0, 3, (3 * (1 - unit.matchups.framesOfSafety / 72)).toInt)
    val desireProfile       = DesireProfile(desireToGoHome, desireForSafety, desireForFreedom)

    if (unit.flying || (unit.transport.exists(_.flying) && unit.matchups.framesOfSafety <= 0)) {
      avoidPotential(unit, desireProfile)
      return
    }
    if (With.configuration.enableThreatAwarePathfinding) {
      avoidRealPath(unit, desireProfile)
    }
    if (unit.unitClass.isReaver && unit.transport.isDefined) {
      DownhillPathfinder.decend(unit, 1, 1, 1, 1)
    }
    if (unit.zone != unit.agent.origin.zone) {
      unit.agent.toTravel = Some(unit.agent.origin)
      Move.delegate(unit)
    }
    avoidPotential(unit, desireProfile)
  }

  val defaultGreedyProfiles = Seq(
    DesireProfile(home = 1, safety = 2, freedom = 1),
    DesireProfile(home = 0, safety = 2, freedom = 1),
    DesireProfile(home = 0, safety = 2, freedom = 0),
    DesireProfile(home = 2, safety = 0, freedom = 1),
    DesireProfile(home = 2, safety = 0, freedom = 0))
  def avoidGreedyPaths(unit: FriendlyUnitInfo, desire: DesireProfile): Unit = {
    defaultGreedyProfiles.sortBy(_.distance(desire)).foreach(someDesire =>
      DownhillPathfinder.decend(
        unit,
        homeValue     = someDesire.home,
        safetyValue   = someDesire.safety,
        freedomValue  = someDesire.freedom,
        targetValue   = someDesire.target))
  }

  def avoidRealPath(unit: FriendlyUnitInfo, desireProfile: DesireProfile): Unit = {

    if (! unit.readyForMicro) return

    val pathLengthMinimum = 3
    val maximumDistance = pathLengthMinimum + Math.max(0, unit.matchups.framesOfEntanglement * unit.topSpeed + unit.effectiveRangePixels).toInt / 32

    val profile = new PathfindProfile(unit.tileIncludingCenter)
    profile.end             = if (desireProfile.home > 0) Some(unit.agent.origin.tileIncluding) else None
    profile.maximumLength   = Some(maximumDistance)
    profile.flying          = unit.flying || unit.transport.exists(_.flying)
    profile.allowGroundDist = true
    profile.costOccupancy   = 0.5f
    profile.costThreat      = 3f - PurpleMath.clamp(desireProfile.home, -1, 1) / 2f
    profile.costRepulsion   = 1f - PurpleMath.clamp(desireProfile.home, -1, 1) / 2f
    profile.repulsors       =
      TakeN
      .by(10, unit.matchups.threats.view)(Ordering.by(t => unit.matchups.framesOfEntanglementPerThreat(t)))
      .map(t => PathfindRepulsor(
        t.pixelCenter,
        t.dpfOnNextHitAgainst(unit),
        64 + t.pixelRangeAgainst(unit)))
      .toIndexedSeq
    profile.unit = Some(unit)
    val path = profile.find

    if (path.pathExists && path.tiles.exists(_.size >= pathLengthMinimum)) {
      unit.agent.path = Some(path)
      path.tiles.get.foreach(With.coordinator.gridPathOccupancy.addUnit(unit, _))
      unit.agent.toTravel = Some(path.tiles.get.take(8).last.pixelCenter)
      Move.delegate(unit)
    }
  }

  def avoidPotential(unit: FriendlyUnitInfo, desireProfile: DesireProfile): Unit = {

    if (! unit.readyForMicro) return

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
