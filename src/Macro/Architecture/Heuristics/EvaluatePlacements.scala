package Macro.Architecture.Heuristics

import Lifecycle.With
import Macro.Architecture.{Blueprint, Placement}
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Tile

import scala.collection.mutable

object EvaluatePlacements {
  
  def best(
    buildingDescriptor  : Blueprint,
    tiles               : Iterable[Tile])
      : Placement = {
    
    // Produce debugging output if visualizations are enabled.
    // Yes, this calculates everything twice.
    
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
      findBest(buildingDescriptor, tiles),
      evaluationsByTile,
      scoresByTile,
      With.frame)
  }
  
  private def findBest(
    buildingDescriptor  : Blueprint,
    tiles               : Iterable[Tile])
      : Option[Tile] = {
    
    if (tiles.isEmpty) {
      return None
    }
    
    // To avoid walling ourselves in, we need to do some potentially expensive calculations.
    // For performance, we delay testing the path-safety of the placement until the very end of the process.
    //
    // So we'll keep popping the best placement off the top until we find one that's path-safe.
      
    val placements = mutable.PriorityQueue[Tile]()(Ordering.by(HeuristicMathMultiplicative.resolve(buildingDescriptor, buildingDescriptor.placement.weightedHeuristics, _)))
    placements ++= tiles
    placements.find(tile => ! With.architecture.breaksPathing(buildingDescriptor.relativeBuildArea.add(tile)))
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
