package Macro.Architecture.Heuristics

import Lifecycle.With
import Macro.Architecture.{BuildingDescriptor, Placement}
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Tile

object EvaluatePlacements {
  
  def best(
    buildingDescriptor  : BuildingDescriptor,
    tiles               : Iterable[Tile])
      : Placement = {
    
    val evaluationsByTile: Iterable[PlacementHeuristicEvaluation] =
      if (With.visualization.enabled)
        tiles.flatMap(tile => evaluate(buildingDescriptor, tile))
      else
        Iterable.empty
    
    Placement(
      buildingDescriptor,
      if (tiles.isEmpty)
        None
      else
        Some(
          HeuristicMathMultiplicative.best(
            buildingDescriptor,
            buildingDescriptor.placement.weightedHeuristics,
            tiles)),
      evaluationsByTile,
      With.frame)
  }
  
  def evaluate(
    buildingDescriptor  : BuildingDescriptor,
    candidate           : Tile)
      : Iterable[PlacementHeuristicEvaluation] =
        buildingDescriptor.placement.weightedHeuristics
          .map(weightedHeuristic =>
            new PlacementHeuristicEvaluation(
              weightedHeuristic.heuristic,
              buildingDescriptor,
              candidate,
              evaluateHeuristic(
                buildingDescriptor,
                candidate,
                weightedHeuristic),
              weightedHeuristic.color))
          
  def evaluateHeuristic(
    buildingDescriptor  : BuildingDescriptor,
    candidate           : Tile,
    heuristicWeight     : PlacementHeuristicWeight)
      : Double =
        heuristicWeight.weighMultiplicatively(
          buildingDescriptor,
          candidate)
}
