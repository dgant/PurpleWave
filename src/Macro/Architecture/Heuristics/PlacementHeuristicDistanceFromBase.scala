package Macro.Architecture.Heuristics

import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile
import Utilities.ByOption

object PlacementHeuristicDistanceFromBase extends PlacementHeuristic {
  
  override def evaluate(blueprint: Blueprint, candidate: Tile): Double = {
  
    if (With.geography.ourBases.isEmpty)
      With.geography.home.pixelCenter.pixelDistance(candidate.pixelCenter)
    else {
      var totalDistance = 0
      val distances = With.geography.ourBases.filter(_.townHall.isDefined).map(base => {
        val from  = base.townHallArea.midPixel
        val to    = candidate.pixelCenter

        var baseDistance = 0.0
        // Performance optimization. Town hall placements matter more so use the zone distance as well.
        if (blueprint.requireTownHallTile.get) {
          baseDistance += from.zone.distancePixels(to.zone)
        }
        baseDistance += from.pixelDistance(to)
        baseDistance
      })

      val distanceSum = distances.sum
      val distanceMin = ByOption.min(distances).getOrElse(candidate.pixelCenter.pixelDistance(With.geography.home.pixelCenter))
      val distanceTotal = distanceMin + distanceSum / (2.0 + With.geography.ourBases.size)
      Math.max(128.0, distanceTotal)
    }
  }
}
