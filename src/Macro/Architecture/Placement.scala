package Macro.Architecture

import Macro.Architecture.Heuristics.PlacementHeuristicEvaluation
import Mathematics.Points.Tile

case class Placement(
  blueprint     : Blueprint,
  tile          : Option[Tile],
  evaluations   : Iterable[PlacementHeuristicEvaluation],
  scoresByTile  : Map[Tile, Double],
  createdFrame  : Int)
