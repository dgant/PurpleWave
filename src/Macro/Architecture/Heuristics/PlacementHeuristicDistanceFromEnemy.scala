package Macro.Architecture.Heuristics

import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

object PlacementHeuristicDistanceFromEnemy extends PlacementHeuristic {
  
  override def evaluate(blueprint: Blueprint, candidate: Tile): Double = {
  
    val enemyBases =
    if (With.geography.enemyBases.isEmpty)
      With.intelligence.leastScoutedBases.filter(_.isStartLocation)
    else
      With.geography.enemyBases
    
    var totalDistance = 0.0
    enemyBases.foreach(base => {
      val from = base.townHallArea.midPixel
      val to = candidate.pixelCenter
  
      // Performance optimization. Town hall placements matter more so use the zone distance as well.
      if (blueprint.requireTownHallTile.get)
        totalDistance += from.zone.distancePixels(to.zone)
      totalDistance += from.pixelDistanceFast(to)
      })
    Math.max(128.0, totalDistance)
  }
}
