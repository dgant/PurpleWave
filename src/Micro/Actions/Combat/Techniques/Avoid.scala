package Micro.Actions.Combat.Techniques

import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.Micro.ShowUnitsFriendly
import Debugging.Visualizations.{Colors, ForceColors}
import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Actions.Combat.Maneuvering.DownhillPathfinder
import Micro.Actions.Combat.Techniques.Common.ActionTechnique
import Micro.Actions.Commands.{Gravitate, Move}
import Micro.Decisions.Potential
import Planning.UnitMatchers.UnitMatchSiegeTank
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

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
    val distanceOriginUs    = unit.pixelDistanceTravelling(unit.agent.origin)
    val distanceOriginEnemy = unit.matchups.threats.view.map(t => t.pixelDistanceTravelling(unit.agent.origin) + t.pixelRangeAgainst(unit)).min
    val enemyCloser         = distanceOriginUs >= distanceOriginEnemy
    val timeOriginUs        = unit.framesToTravelTo(unit.agent.origin)
    val timeOriginEnemy     = unit.matchups.threats.view.map(t => t.framesToTravelTo(unit.agent.origin) - t.pixelRangeAgainst(unit)).min
    val enemySooner         = timeOriginUs >= timeOriginEnemy
    val enemySieging        = unit.matchups.enemies.exists(_.isAny(UnitMatchSiegeTank, Zerg.Lurker))
    val atHome              = unit.zone == unit.agent.origin.zone
    val scouting            = unit.agent.canScout
    val desireToGoHome      = if (enemySieging) -1 else if (scouting || atHome) 0 else (
      (if (enemyCloser) 1 else 0) + (if (enemySooner) 1 else 0)
    )
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

  val defaultProfiles = Seq(
    DesireProfile(home = 1, safety = 2, freedom = 1),
    DesireProfile(home = 0, safety = 2, freedom = 1),
    DesireProfile(home = 0, safety = 2, freedom = 0),
    DesireProfile(home = 2, safety = 0, freedom = 1),
    DesireProfile(home = 2, safety = 0, freedom = 0))

  def avoidGreedyPaths(unit: FriendlyUnitInfo, desire: DesireProfile): Unit = {
    defaultProfiles.sortBy(_.distance(desire)).foreach(someDesire =>
      DownhillPathfinder.decend(
        unit,
        homeValue     = someDesire.home,
        safetyValue   = someDesire.safety,
        freedomValue  = someDesire.freedom,
        targetValue   = someDesire.target))
  }

  def avoidRealPath(unit: FriendlyUnitInfo, desireProfile: DesireProfile): Unit = {

    if (! unit.readyForMicro) return

    val end = if (desireProfile.home > 0) Some(unit.agent.origin.tileIncluding) else None
    val maximumDistance = 3 + Math.max(0, unit.matchups.framesOfEntanglement * unit.topSpeed + unit.effectiveRangePixels).toInt / 32

    val path = With.paths.profileThreatAware(
      start = unit.tileIncludingCenter,
      end = end,
      goalDistance = maximumDistance,
      flying = unit.flying || unit.transport.nonEmpty).find

    if (path.pathExists && path.tiles.exists(_.size > 3)) {
      if (ShowUnitsFriendly.inUse && With.visualization.map) {
        for (i <- 0 until path.tiles.get.size - 1) {
          DrawMap.arrow(
            path.tiles.get(i).pixelCenter,
            path.tiles.get(i + 1).pixelCenter,
            Colors.White)
        }
      }
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
