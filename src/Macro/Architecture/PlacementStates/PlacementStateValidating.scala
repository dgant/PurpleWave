package Macro.Architecture.PlacementStates

import Lifecycle.With
import Macro.Architecture.{Placement, PlacementSuggestion}
import Mathematics.Points.Tile

import scala.collection.mutable

class PlacementStateValidating(placementSuggestion: PlacementSuggestion) extends PlacementState {
  override def step() {
    val blueprint = placementSuggestion.blueprint
    if (blueprint.forcePlacement) {
      val placement = Placement(
        blueprint,
        Some(blueprint.requireCandidates.get.head),
        Seq.empty,
        new mutable.HashMap[Tile, Double],
        totalNanoseconds  = 0,
        frameStarted      = With.frame,
        frameFinished     = With.frame,
        candidates        = 1,
        evaluated         = 1)
      With.placement.usePlacement(placementSuggestion, placement)
      transition(new PlacementStateReady)
    } else {
      transition(new PlacementStateEvaluating(placementSuggestion))
    }
  }
}
