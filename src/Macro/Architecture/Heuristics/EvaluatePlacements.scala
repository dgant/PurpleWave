package Macro.Architecture.Heuristics

import Lifecycle.With
import Macro.Architecture.{Blueprint, Placement}
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Tile

object EvaluatePlacements {
  
  def best(
    buildingDescriptor  : Blueprint,
    tiles               : Iterable[Tile])
      : Placement = {
    
    // Produce debugging output if visualizations are enabled.
    // Yes, this calculates everything twice
    
    val evaluationsByTile: Iterable[PlacementHeuristicEvaluation] =
      if (With.visualization.enabled)
        tiles.flatMap(tile => evaluate(buildingDescriptor, tile))
      else
        Iterable.empty
    
    val scoresByTile: Map[Tile, Double] =
      if (With.visualization.enabled)
        tiles.map(tile =>
          (tile,
            HeuristicMathMultiplicative.resolve(
              buildingDescriptor,
              buildingDescriptor.placement.weightedHeuristics,
              tile))
          ).toMap
      else
        Map.empty
    
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
      scoresByTile,
      With.frame)
  }
  
  def evaluate(
                buildingDescriptor  : Blueprint,
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
                         buildingDescriptor  : Blueprint,
                         candidate           : Tile,
                         heuristicWeight     : PlacementHeuristicWeight)
      : Double =
        heuristicWeight.weighMultiplicatively(
          buildingDescriptor,
          candidate)
}
