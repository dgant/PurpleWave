package Micro.Heuristics.Movement

import Mathematics.Shapes.Circle
import Lifecycle.With
import Mathematics.Heuristics.HeuristicMath
import Micro.Heuristics.MovementHeuristics.MovementHeuristicResult
import Micro.Intent.Intention
import Utilities.EnrichPosition._
import bwapi.TilePosition

object EvaluateMoves {
  
  def best(
    intent:Intention,
    profile:MovementProfile,
    searchRange:Int)
      :TilePosition = {
    
    val candidates =
      Circle.points(searchRange)
        .map(intent.unit.tileIncluding.add)
        .filter(_.valid)
        .filter(intent.unit.canTraverse)
  
    if (candidates.isEmpty) {
      return intent.unit.tileIncluding
    }
    
    if (With.configuration.visualizeHeuristicMovement) {
      With.executor.getState(intent.unit).movementHeuristicResults =
        candidates.flatten(candidate =>
          profile.weightedHeuristics.map(weightedHeuristic =>
            new MovementHeuristicResult(
              weightedHeuristic.heuristic,
              intent,
              candidate,
              weightedHeuristic.weigh(intent, candidate),
              weightedHeuristic.color)))
    }
    
    HeuristicMath.calculateBest(intent, profile.weightedHeuristics, candidates)
  }
}
