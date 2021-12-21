package Macro.Architecture.Heuristics

import Macro.Architecture.Blueprint
import Placement.Heuristics.{Heuristic, HeuristicWeight}
import Mathematics.Points.Tile
import bwapi.Color

class PlacementHeuristicWeight(
                                heuristic : Heuristic[Blueprint, Tile],
                                weight    : Double,
                                val color : Color)

  extends HeuristicWeight(heuristic, weight)