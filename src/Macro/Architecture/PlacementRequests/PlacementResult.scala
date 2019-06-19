package Macro.Architecture.PlacementRequests

import Lifecycle.With
import Mathematics.Points.Tile

case class PlacementResult(
  request           : PlacementRequest,
  var tile          : Option[Tile] = None,
  var frameStarted  : Int = With.frame,
  var frameFinished : Int = With.frame,
  var candidates    : Int = 0,
  var evaluated     : Int = 0)