package Macro.Architecture.Heuristics

import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

import scala.collection.mutable

object EvaluatePlacements {
  
  def findBest(
    blueprint: Blueprint,
    evaluations: Map[Tile, Double])
      : Option[Tile] = {
    
    if (evaluations.isEmpty) {
      return None
    }
  
    // To avoid walling ourselves in, we need to do some potentially expensive calculations.
    // For performance, we delay testing the path-safety of the placement until the very end of the process.
    //
    // So we'll keep popping the best placement off the top until we find one that's path-safe.
  
    val placements = mutable.PriorityQueue[Tile]()(Ordering.by( - evaluations(_)))
    placements ++= evaluations.keys
    placements.find(tile => ! With.architecture.breaksPathing(blueprint.relativeBuildArea.add(tile)))
  }
  
  def evaluate(
    blueprint : Blueprint,
    candidate : Tile)
      : Iterable[PlacementHeuristicEvaluation] =
        blueprint.placementProfile.get.weightedHeuristics
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
        heuristicWeight.weighMultiplicatively(
          buildingDescriptor,
          candidate)
}
