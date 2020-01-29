package Micro.Actions.Combat.Maneuvering

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.Micro.ShowUnitsFriendly
import Lifecycle.With
import Mathematics.Points.Tile
import Mathematics.PurpleMath
import Mathematics.Shapes.Ring
import Micro.Actions.Commands.Move
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable.ArrayBuffer

object DownhillPathfinder {

  def decend(
    unit          : FriendlyUnitInfo,
    homeValue     : Int = 1,
    safetyValue   : Int = 1,
    freedomValue  : Int = 1,
    targetValue   : Int = 0)
      : Unit = {

    if (! unit.ready) return

    val enemyRangeGrid = unit.enemyRangeGrid

    var pathLengthMax = PurpleMath.clamp(unit.matchups.framesOfEntanglement * unit.topSpeed + 3, 6, 10)
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

    val idealEnemyVulnerability = With.grids.enemyVulnerabilityGround.rangeMax - unit.pixelRangeGround.toInt / 32 - 1 // The -1 is a safety buffer
    def tileScore(tile: Tile): Int = {
      val enemyRange = enemyRangeGrid.get(tile)
      (
        - 10 * homeValue * tileDistance(tile)
        - 10 * safetyValue * enemyRange * (if (enemyRange > enemyRangeGrid.addedRange) 2 else 1)
        - (if (unit.cloaked)
          10 * safetyValue * With.grids.enemyDetection.get(tile) * (if (With.grids.enemyDetection.isDetected(tile)) 2 else 1)
          else 0)
        - 10 * targetValue * Math.abs(idealEnemyVulnerability - With.grids.enemyVulnerabilityGround.get(tile))
        - freedomValue * PurpleMath.clamp(With.coordinator.gridPathOccupancy.get(tile) / 3, 0, 9)
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

    if (path.length >= Math.max(2, enemyRangeGrid.get(unit.tileIncludingCenter))) {
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
}
