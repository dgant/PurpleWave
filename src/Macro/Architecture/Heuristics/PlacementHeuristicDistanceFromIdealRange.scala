package Macro.Architecture.Heuristics

import Macro.Architecture.Blueprint
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Tile

object PlacementHeuristicDistanceFromIdealRange extends PlacementHeuristic {
  
  override def evaluate(blueprint: Blueprint, candidate: Tile): Double = {
    
    val targetDistance  = blueprint.marginPixels.get
    val candidatePixel  = candidate.topLeftPixel.add(16 * blueprint.widthTiles.get, 16 * blueprint.heightTiles.get)
    val zone            = candidate.zone
    
    if (zone.exit.isEmpty) HeuristicMathMultiplicative.default
    
    // TODO: Once we add some better math tools use distance from line instead
    
    Math.abs(targetDistance - candidatePixel.pixelDistance(zone.exit.get.pixelCenter))
  }
}
