package Macro.Architecture.PlacementStates

import Lifecycle.With
import Macro.Architecture.{PlacementResult, PlacementRequest}
import Mathematics.Points.Tile

import scala.collection.mutable

class PlacementStateValidating(request: PlacementRequest) extends PlacementState {
  override def step() {
    if (request.tile.exists(request.blueprint.accepts(_))) {
      val result = PlacementResult(
        request.blueprint,
        request.tile,
        Seq.empty,
        new mutable.HashMap[Tile, Double],
        totalNanoseconds  = 0,
        frameStarted      = With.frame,
        frameFinished     = With.frame,
        candidates        = 1,
        evaluated         = 1)
      With.placement.usePlacement(request, result)
      transition(new PlacementStateReady)
    } else {
      transition(new PlacementStateEvaluating(request))
    }
  }
}
