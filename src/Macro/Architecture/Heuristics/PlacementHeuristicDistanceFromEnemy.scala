package Macro.Architecture.Heuristics

import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

object PlacementHeuristicDistanceFromEnemy extends PlacementHeuristic {
  
  override def evaluate(blueprint: Blueprint, candidate: Tile): Double = {
  
    val enemyBases =
    if (With.geography.enemyBases.isEmpty)
      With.geography.startBases.filterNot(_.owner.isUs)
    else
      With.geography.enemyBases
    
    var totalDistance = 0.0
    enemyBases.foreach(base => {
      val from = base.townHallArea.midPixel
      val to = candidate.pixelCenter
  
      // Performance optimization.
      if (blueprint.requireTownHallTile.get || blueprint.building.exists(_.dealsDamage)) {
        totalDistance += from.zone.distancePixels(to.zone)
      }
      
      totalDistance += from.pixelDistance(to)
    })
    Math.max(128.0, totalDistance)
  }
}
