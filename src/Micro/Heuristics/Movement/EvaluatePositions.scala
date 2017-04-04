package Micro.Heuristics.Movement

import Debugging.Visualization.Data.MovementHeuristicView
import Geometry.Shapes.Circle
import Lifecycle.With
import Micro.Intent.Intention
import Utilities.EnrichPosition._
import bwapi.TilePosition

object EvaluatePositions {
  
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
      //Weird. unit is nowhere near a traversible position
      return intent.unit.tileCenter
    }
  
    val weightedHeuristics = profile.heuristics
    
    //Debug heuristics on selected units
    //Yes, this re-weighs everything for the selected unit(s) as a development shortcut.
    if (With.configuration.enableVisualizationMovementHeuristics) {
      With.executor.getState(intent.unit).movementHeuristics =
        candidates.flatten(candidate =>
          weightedHeuristics.map(weightedHeuristic =>
            new MovementHeuristicView(weightedHeuristic, intent, candidate, weightedHeuristic.weigh(intent, candidate))))
    }
    
    candidates.maxBy(candidate => weightedHeuristics.map(_.weigh(intent, candidate)).product)
  }
}
