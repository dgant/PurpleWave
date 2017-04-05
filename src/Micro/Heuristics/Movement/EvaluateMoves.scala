package Micro.Heuristics.Movement

import Geometry.Shapes.Circle
import Lifecycle.With
import Micro.Heuristics.General.HeuristicMath
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
        .map(intent.unit.tileCenter.add)
        .filter(_.valid)
        .filter(intent.unit.canTraverse)
  
    if (candidates.isEmpty) {
      return intent.unit.tileCenter
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
    
    HeuristicMath.weigh(intent, profile.weightedHeuristics, candidates)
  }
}
