package Macro.Architecture.Heuristics

import Macro.Architecture.Blueprint
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Tile

object PlacementHeuristicNatural extends PlacementHeuristic {
  
  override def evaluate(building: Blueprint, candidate: Tile): Double = {
    
    HeuristicMathMultiplicative.fromBoolean(candidate.zone.bases.exists(_.isNaturalOf.exists(_.owner.isUs)))
  }
}
