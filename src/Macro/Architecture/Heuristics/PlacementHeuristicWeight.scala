package Macro.Architecture.Heuristics

import Macro.Architecture.BuildingDescriptor
import Mathematics.Heuristics.{Heuristic, HeuristicWeight}
import Mathematics.Points.Tile

class PlacementHeuristicWeight(
  heuristic : Heuristic[BuildingDescriptor, Tile],
  weight    : Double)

  extends HeuristicWeight(heuristic, weight)