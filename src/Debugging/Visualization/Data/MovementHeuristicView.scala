package Debugging.Visualization.Data

import Micro.Heuristics.Movement.WeightedMovementHeuristic
import Micro.Intentions.Intention
import bwapi.TilePosition

class MovementHeuristicView(
  val heuristic : WeightedMovementHeuristic,
  val intent    : Intention,
  val candidate : TilePosition) {
  
}
