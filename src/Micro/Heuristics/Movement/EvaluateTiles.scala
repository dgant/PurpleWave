package Micro.Heuristics.Movement

import Lifecycle.With
import Mathematics.Heuristics.HeuristicMath
import Mathematics.Pixels.{Point, Tile}
import Micro.Heuristics.MovementHeuristics.MovementHeuristicResult
import Micro.Intent.Intention

object EvaluateTiles {
  
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
  
  def best(intent:Intention, profile:MovementProfile): Tile = {
    
    val candidates =
      points
        .map(intent.unit.tileIncludingCenter.add)
        .filter(_.valid)
        .filter(intent.unit.canTraverse)
  
    if (candidates.isEmpty) {
      return intent.unit.tileIncludingCenter
    }
    
    if (With.configuration.visualizeHeuristicMovement) {
      intent.state.movementHeuristicResults =
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
