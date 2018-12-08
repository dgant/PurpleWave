package Macro.Architecture.Heuristics

import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

object EvaluatePlacements {

  def evaluate(
    blueprint : Blueprint,
    candidate : Tile)
      : Iterable[PlacementHeuristicEvaluation] =
        blueprint.placement.get.weightedHeuristics
          .map(weightedHeuristic =>
            new PlacementHeuristicEvaluation(
              weightedHeuristic.heuristic,
              blueprint,
              candidate,
              evaluateHeuristic(
                blueprint,
                candidate,
                weightedHeuristic),
              weightedHeuristic.color))
          
  def evaluateHeuristic(
    buildingDescriptor  : Blueprint,
    candidate           : Tile,
    heuristicWeight     : PlacementHeuristicWeight)
      : Double =
        heuristicWeight.apply(buildingDescriptor, candidate)
}
