package Macro.Architecture.PlacementState

import Lifecycle.With
import Macro.Architecture.{Blueprint, Placement}

import scala.collection.mutable

abstract class PlacementState {
  final def queue       : mutable.Queue[Blueprint]  = With.placement.queue
  final def placements  : Map[Blueprint, Placement] = With.placement.placements
  final def transition(state: PlacementState) {
    With.placement.setState(state)
  }
  
  def isComplete: Boolean = false
  def step() {}
}
