package Macro.Architecture.Heuristics

import Macro.Architecture.Blueprint
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Maff
import Mathematics.Points.Tile


object PlacementHeuristicResources extends PlacementHeuristic {
  
  override def evaluate(blueprint: Blueprint, candidate: Tile): Double = {
    
    val zone = candidate.zone
    val base = Maff.minBy(zone.bases)(_.townHallTile.tileDistanceManhattan(candidate))
    
    if (base.isEmpty) return HeuristicMathMultiplicative.default
    
    base.get.mineralsLeft + 6 * base.get.gasLeft
  }
}
