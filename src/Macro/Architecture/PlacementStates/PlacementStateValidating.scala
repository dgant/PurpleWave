package Macro.Architecture.PlacementStates

import Lifecycle.With
import Macro.Architecture.{Blueprint, Placement}
import Mathematics.Points.Tile

import scala.collection.mutable

class PlacementStateValidating(blueprint: Blueprint) extends PlacementState {
  override def step() {
    val placement = placements.get(blueprint)
    if (placement.exists(_.satisfies(blueprint))) {
      With.architecture.assumePlacement(placement.get)
      transition(new PlacementStateReady)
    } else if (blueprint.forcePlacement) {
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
      With.architecture.assumePlacement(placement)
      With.groundskeeper.updatePlacement(blueprint, placement)
      transition(new PlacementStateReady)
    } else {
      transition(new PlacementStateEvaluating(blueprint))
    }
  }
}
