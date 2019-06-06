package Macro.Architecture.PlacementStates

import Lifecycle.With
import Macro.Allocation.ArchitectureState2
import Macro.Architecture.{Blueprint, Placement}

import scala.collection.mutable

abstract class PlacementState {
  final def queue       : mutable.Queue[Blueprint]  = if (ArchitectureState2.enabled) ArchitectureState2.queue else With.placement.queue
  final def placements  : Map[Blueprint, Placement] = if (ArchitectureState2.enabled) ArchitectureState2.placements else With.placement.placements
  final def transition(state: PlacementState) {
    if (ArchitectureState2.enabled) {
      ArchitectureState2.setState(state)
    } else {
      With.placement.setState(state)
    }
  }
  
  def isComplete: Boolean = false
  def step() {}
}
