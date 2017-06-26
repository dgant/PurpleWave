package Macro.Architecture

import Macro.Architecture.Heuristics.PlacementHeuristicEvaluation
import Mathematics.Points.Tile

case class Placement(
                      buildingDescriptor  : BuildingDescriptor,
                      tile                : Option[Tile],
                      evaluations             : Iterable[PlacementHeuristicEvaluation],
                      createdFrame        : Int)
