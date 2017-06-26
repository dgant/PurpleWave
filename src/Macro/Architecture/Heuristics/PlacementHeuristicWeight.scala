package Macro.Architecture.Heuristics

import Macro.Architecture.BuildingDescriptor
import Mathematics.Heuristics.{Heuristic, HeuristicWeight}
import Mathematics.Points.Tile
import bwapi.Color

class PlacementHeuristicWeight(
  heuristic : Heuristic[BuildingDescriptor, Tile],
  weight    : Double,
  val color : Color)

  extends HeuristicWeight(heuristic, weight)