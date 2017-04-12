package Micro.Heuristics.Movement

import Lifecycle.With
import Mathematics.Heuristics.HeuristicMath
import Mathematics.Positions.Point
import Micro.Heuristics.MovementHeuristics.MovementHeuristicResult
import Micro.Intent.Intention
import Utilities.EnrichPosition._
import bwapi.TilePosition

object EvaluateMoves {
  
  val points = Vector(
    new Point(-1, -2),
    new Point( 0, -2),
    new Point( 1, -2),
    new Point(-2, -1),
    new Point(-1, -1),
    new Point( 0, -1),
    new Point( 1, -1),
    new Point( 2, -1),
    new Point(-2,  0),
    new Point(-1,  0),
    new Point( 0,  0),
    new Point( 1,  0),
    new Point( 2,  0),
    new Point(-2,  1),
    new Point(-1,  1),
    new Point( 0,  1),
    new Point( 1,  1),
    new Point( 2,  1),
    new Point(-1,  2),
    new Point( 0,  2),
    new Point( 1,  2)
  )
  
  def best(
    intent:Intention,
    profile:MovementProfile)
      :TilePosition = {
    
    val candidates =
      points
        .map(intent.unit.tileIncludingCenter.add)
        .filter(_.valid)
        .filter(intent.unit.canTraverse)
  
    if (candidates.isEmpty) {
      return intent.unit.tileIncludingCenter
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
