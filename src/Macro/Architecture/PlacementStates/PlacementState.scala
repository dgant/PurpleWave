package Macro.Architecture.PlacementStates

import Macro.Allocation.Placer
import Macro.Architecture.Blueprint

import scala.collection.mutable

abstract class PlacementState {
  final def queue: mutable.Queue[Blueprint] = Placer.queue
  final def transition(state: PlacementState): Unit = Placer.setState(state)
  
  def isComplete: Boolean = false
  def step() {}
}
