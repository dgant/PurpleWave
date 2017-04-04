package Debugging.Visualization.Data

import Micro.Heuristics.Movement.WeightedMovementHeuristic
import Micro.Intent.Intention
import bwapi.TilePosition

class MovementHeuristicView(
  val heuristic   : WeightedMovementHeuristic,
  val intent      : Intention,
  val candidate   : TilePosition,
  val evaluation  : Double)