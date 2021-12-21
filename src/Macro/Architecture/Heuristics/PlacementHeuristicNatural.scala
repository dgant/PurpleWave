package Macro.Architecture.Heuristics

import Lifecycle.With
import Macro.Architecture.Blueprint
import Placement.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Tile

object PlacementHeuristicNatural extends PlacementHeuristic {
  
  override def evaluate(blueprint: Blueprint, candidate: Tile): Double = {
    HeuristicMathMultiplicative.fromBoolean(candidate.zone.bases.exists(_.isNaturalOf.contains(With.geography.ourMain)))
  }
}
