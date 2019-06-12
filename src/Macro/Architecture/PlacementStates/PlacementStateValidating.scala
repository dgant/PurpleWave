package Macro.Architecture.PlacementStates

import Lifecycle.With
import Macro.Allocation.Placer
import Macro.Architecture.{Blueprint, Placement}
import Mathematics.Points.Tile

import scala.collection.mutable

class PlacementStateValidating(blueprint: Blueprint) extends PlacementState {
  override def step() {
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
      Placer.addPlacement(placement)
      With.architecture.assumePlacement(placement)
      transition(new PlacementStateReady)
    } else {
      transition(new PlacementStateEvaluating(blueprint))
    }
  }
}
