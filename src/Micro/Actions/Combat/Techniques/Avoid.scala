package Micro.Actions.Combat.Techniques

import Debugging.Visualizations.{Colors, ForceColors}
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.Micro.ShowUnitsFriendly
import Lifecycle.With
import Mathematics.Points.Tile
import Mathematics.PurpleMath
import Mathematics.Shapes.Ring
import Micro.Actions.Combat.Techniques.Common.ActionTechnique
import Micro.Actions.Commands.{Gravitate, Move}
import Micro.Decisions.Potential
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

import scala.collection.mutable.ArrayBuffer

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

  override def perform(unit: FriendlyUnitInfo): Unit = {
    if (unit.flying) {
      avoidPotential(unit)
      return
    }
    if (With.configuration.enableThreatAwarePathfinding) {
      avoidRealPath(unit)
    }
    if (unit.readyForMicro) {
      avoidGreedyPath(unit)
    }
    if (unit.readyForMicro) {
      avoidPotential(unit)
    }
  }

  def avoidRealPath(unit: FriendlyUnitInfo): Unit = {
    val path = With.paths.aStarThreatAware(unit, if (unit.agent.origin.zone == unit.zone) None else Some(unit.agent.origin.tileIncluding))
      if (path.pathExists && path.tiles.exists(_.size > 3)) {
        // Path tiles are in REVERSE
        if (ShowUnitsFriendly.inUse && With.visualization.map) {
          for (i <- 0 until path.tiles.get.size - 1) {
            DrawMap.arrow(
              path.tiles.get(i + 1).pixelCenter,
              path.tiles.get(i).pixelCenter,
              Colors.MediumOrange)
          }
        }
        path.tiles.get.foreach(With.coordinator.gridPathOccupancy.addUnit(unit, _))
        unit.agent.toTravel = Some(path.end.pixelCenter)
        Move.delegate(unit)
        return
      }
  }

  def avoidGreedyPath(unit: FriendlyUnitInfo): Unit = {
    var pathLengthMax = 8
    val path = new ArrayBuffer[Tile]
    path += unit.tileIncludingCenter
    var bestScore = Int.MinValue
    def tileDistance(tile: Tile): Int =
      if (unit.agent.canScout)
        0
      else if(tile.zone == unit.agent.origin.zone)
        0
      else
        unit.agent.origin.zone.distanceGrid.get(tile)
    def tileScore(tile: Tile): Int = {
      val enemyRange = With.grids.enemyRange.get(tile)
      (
        - 10 * tileDistance(tile)
        - 10 * enemyRange * (if (enemyRange > With.grids.enemyRange.addedRange) 2 else 1)
        - (if (unit.cloaked)
          10 * With.grids.enemyDetection.get(tile) * (if (With.grids.enemyDetection.isDetected(tile)) 2 else 1)
          else 0)
        - PurpleMath.clamp(With.coordinator.gridPathOccupancy.get(tile) / 3, 0, 9)
      )
    }
    val directions = Ring.points(1)
    var directionModifier = 0 // Rotate the first direction we try to discover diagonals
    while (path.length < pathLengthMax) {
      val origin = unit.agent.origin.zone
      val here = path.last
      var bestScore = tileScore(here)
      var bestTile = here
      directionModifier += 1
      var iDirection = 0
      while (iDirection < 4) {
        val there = here.add(directions((iDirection + directionModifier) % 4))
        iDirection += 1
        if (there.valid && With.grids.walkable.get(there) && ! path.contains(there)) {
          val score = tileScore(there)
          if (score > bestScore) {
            bestScore = score
            bestTile = there
          }
        }
      }
      if (bestTile == here) {
        // Makeshift "break"
        pathLengthMax = -1
      } else {
        path += bestTile
      }
    }

    if (path.length >= Math.max(2, With.grids.enemyRange.get(unit.tileIncludingCenter))) {
        if (ShowUnitsFriendly.inUse && With.visualization.map) {
          for (i <- 0 until path.length - 1) {
            DrawMap.arrow(
              path(i).pixelCenter,
              path(i + 1).pixelCenter,
              Colors.DarkTeal)
          }
        }
        path.foreach(With.coordinator.gridPathOccupancy.addUnit(unit, _))
        unit.agent.toTravel = Some(path.last.pixelCenter)
      Move.delegate(unit)
    }
  }

  def avoidPotential(unit: FriendlyUnitInfo): Unit = {
    unit.agent.toTravel = Some(unit.agent.origin)

    val bonusAvoidThreats = PurpleMath.clamp(With.reaction.agencyAverage + unit.matchups.framesOfEntanglement, 12.0, 24.0) / 12.0
    val bonusPreferExit   = if (unit.agent.origin.zone != unit.zone) 1.0 else if (unit.matchups.threats.exists(_.topSpeed < unit.topSpeed)) 0.0 else 0.5
    val bonusRegrouping   = 9.0 / Math.max(24.0, unit.matchups.framesOfEntanglement)
    val bonusMobility     = 1.0

    val forceThreat       = Potential.avoidThreats(unit)      * bonusAvoidThreats
    val forceSpacing      = Potential.avoidCollision(unit)
    val forceExiting      = Potential.preferTravelling(unit)  * bonusPreferExit
    val forceSpreading    = Potential.preferSpreading(unit)
    val forceRegrouping   = Potential.preferRegrouping(unit)  * bonusRegrouping
    val forceMobility     = Potential.preferMobility(unit)    * bonusMobility
    val forceSneaking     = Potential.detectionRepulsion(unit)
    val resistancesTerran = Potential.resistTerrain(unit)
    
    unit.agent.forces.put(ForceColors.threat,         forceThreat)
    unit.agent.forces.put(ForceColors.traveling,      forceExiting)
    unit.agent.forces.put(ForceColors.spreading,      forceSpreading)
    unit.agent.forces.put(ForceColors.regrouping,     forceRegrouping)
    unit.agent.forces.put(ForceColors.spacing,        forceSpacing)
    unit.agent.forces.put(ForceColors.mobility,       forceMobility)
    unit.agent.forces.put(ForceColors.sneaking,       forceSneaking)
    unit.agent.resistances.put(ForceColors.mobility,  resistancesTerran)
    Gravitate.delegate(unit)

    Move.delegate(unit)
  }
}
