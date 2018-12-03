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
  
  // If our path home is blocked by enemies,
  // try to find an alternate escape route.
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.matchups.threats.nonEmpty
  )
  
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
      oldPerform(unit)
      return
    }

    var pathLengthMax = 6
    val path = new ArrayBuffer[Tile]
    path += unit.tileIncludingCenter
    var bestScore = Int.MinValue
    def tileDistance(tile: Tile): Int = if(tile.zone == unit.agent.origin.zone) 0 else unit.agent.origin.zone.distanceGrid.get(tile)
    def tileScore(tile: Tile): Int = {
      val enemyRange = With.grids.enemyRange.get(tile)
      (
        - 20 * tileDistance(tile)
        - 20 * enemyRange * (if (enemyRange > With.grids.enemyRange.addedRange) 2 else 1)
        - PurpleMath.clamp(With.grids.occupancy(tile) / 2, 0, 4)
        - PurpleMath.clamp(With.coordinator.gridPathOccupancy.get(tile) / 2, 0, 15)
      )
    }
    while (path.length < pathLengthMax) {
      val origin = unit.agent.origin.zone
      val here = path.last
      var bestScore = tileScore(here)
      var bestTile = here
      for (p <- Ring.points(1)) {
        val there = here.add(p)
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

    unit.agent.toTravel = {
      if (path.length < Math.max(2, With.grids.enemyRange.get(unit.tileIncludingCenter))) {
        // We're stuck. Just go home.
        Some(unit.agent.origin)
      } else {
        if (ShowUnitsFriendly.inUse) {
          for (i <- 0 until path.length - 1) {
            DrawMap.arrow(path(i).pixelCenter, path(i + 1).pixelCenter, Colors.DarkTeal)
          }
        }
        path.foreach(With.coordinator.gridPathOccupancy.addUnit(unit, _))
        Some(path.last.pixelCenter)
      }
    }
    Move.delegate(unit)
  }
  
  def oldPerform(unit: FriendlyUnitInfo): Unit = {
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
    val resistancesTerran = Potential.resistTerrain(unit)
    
    unit.agent.forces.put(ForceColors.threat,         forceThreat)
    unit.agent.forces.put(ForceColors.traveling,      forceExiting)
    unit.agent.forces.put(ForceColors.spreading,      forceSpreading)
    unit.agent.forces.put(ForceColors.regrouping,     forceRegrouping)
    unit.agent.forces.put(ForceColors.spacing,        forceSpacing)
    unit.agent.forces.put(ForceColors.mobility,       forceMobility)
    unit.agent.resistances.put(ForceColors.mobility,  resistancesTerran)
    Gravitate.delegate(unit)

    Move.delegate(unit)
  }
}
