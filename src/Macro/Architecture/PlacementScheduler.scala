package Macro.Architecture

import Lifecycle.With
import Macro.Architecture.PlacementState.{PlacementState, StateComplete, StateInitial}

import scala.collection.mutable

class PlacementScheduler {
  
  var queue       : mutable.Queue[Blueprint]  = _
  var placements  : Map[Blueprint, Placement] = _
  var state       : PlacementState            = new StateComplete
  
  def run() {
    if (state.isComplete) {
      reset()
      setState(new StateInitial)
    }
    while (With.performance.continueRunning && ! state.isComplete) {
      state.step()
    }
  }
  
  def setState(newState: PlacementState) {
    state = newState
  }
  
  private def reset() {
    queue = new mutable.Queue[Blueprint] ++ With.groundskeeper.proposalQueue
    placements = With.groundskeeper.proposalPlacements.toMap
  }
}
