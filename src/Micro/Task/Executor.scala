package Micro.Task

import Lifecycle.With
import Micro.Actions.Idle
import Micro.Intent.Intention
import Performance.Batching.BatchProcessor
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

class Executor extends BatchProcessor[FriendlyUnitInfo] {
  
  private val stateByUnit = new mutable.HashMap[FriendlyUnitInfo, ExecutionState]
  
  def states: Iterable[ExecutionState] = stateByUnit.values
  
  def intend(intention:Intention) = {
    getState(intention.unit).intent = intention
  }
  
  def getState(unit:FriendlyUnitInfo):ExecutionState = {
    if ( ! stateByUnit.contains(unit)) {
      stateByUnit.put(unit, new ExecutionState(unit))
    }
    stateByUnit(unit)
  }
  
  ///////////////
  // BatchTask //
  ///////////////
  
  override protected def shouldRun(): Boolean = With.latency.isLastFrameOfTurn
  
  override protected def onRun() {
    stateByUnit.keys.filterNot(_.alive).foreach(stateByUnit.remove)
  }
  
  override def onPopulate(): Traversable[FriendlyUnitInfo] = stateByUnit.keys.toVector.sortBy(getState(_).lastFrame)
  
  override def onNext(item: FriendlyUnitInfo) {
    Idle.consider(getState(item))
  }
}
