package Macro.Architecture

import Macro.Architecture.Heuristics.PlacementHeuristicEvaluation
import Mathematics.Points.Tile

import scala.collection.mutable

case class PlacementResult(
  request           : PlacementRequest,
  tile              : Option[Tile],
  evaluations       : Iterable[PlacementHeuristicEvaluation],
  scoresByTile      : mutable.HashMap[Tile, Double],
  totalNanoseconds  : Long,
  frameStarted      : Int,
  frameFinished     : Int,
  candidates        : Int,
  evaluated         : Int)