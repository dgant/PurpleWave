package Macro.Architecture.PlacementRequests

import Macro.Architecture.Heuristics.PlacementHeuristicEvaluation
import Mathematics.Points.Tile

case class PlacementResult(
  request           : PlacementRequest,
  tile              : Option[Tile],
  totalNanoseconds  : Long,
  frameStarted      : Int,
  frameFinished     : Int,
  candidates        : Int,
  evaluated         : Int)