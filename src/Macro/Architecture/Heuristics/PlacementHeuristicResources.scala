package Macro.Architecture.Heuristics

import Macro.Architecture.Blueprint
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Tile
import Utilities.ByOption

object PlacementHeuristicResources extends PlacementHeuristic {
  
  override def evaluate(blueprint: Blueprint, candidate: Tile): Double = {
    
    val zone = candidate.zone
    val base = ByOption.minBy(zone.bases)(_.townHallTile.tileDistanceManhattan(candidate))
    
    if (base.isEmpty) return HeuristicMathMultiplicative.default
    
    base.get.mineralsLeft + 4 * base.get.gasLeft
  }
}
