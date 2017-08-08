package Macro.Architecture

import Lifecycle.With
import Macro.Architecture.PlacementStates.{PlacementState, PlacementStateComplete, PlacementStateInitial}

import scala.collection.mutable

class PlacementScheduler {
  
  var queue       : mutable.Queue[Blueprint]  = _
  var placements  : Map[Blueprint, Placement] = _
  var state       : PlacementState            = new PlacementStateComplete
  
  def run(runToCompletionEvenIfItCostsUsAFrame: Boolean = false) {
    if (state.isComplete) {
      reset()
      setState(new PlacementStateInitial)
    }
    while ( ! state.isComplete && (runToCompletionEvenIfItCostsUsAFrame || With.performance.continueRunning)) {
      state.step()
    }
  }
  
  def setState(newState: PlacementState) {
    state = newState
  }
  
  private def reset() {
    queue       = new mutable.Queue[Blueprint] ++ With.groundskeeper.proposalQueue
    placements  = With.groundskeeper.proposalPlacements.toMap
  }
}
