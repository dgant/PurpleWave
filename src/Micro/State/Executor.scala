package Micro.State

import Lifecycle.With
import Micro.Intent.Intention
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

class Executor {
  
  private val stateByUnit = new mutable.HashMap[FriendlyUnitInfo, ExecutionState]
  
  def states: Iterable[ExecutionState] = stateByUnit.values
  
  def intend(intention:Intention) = {
    getState(intention.unit).intent = intention
  }
  
  def onFrame() {
    
    stateByUnit
      .keys
      .filterNot(_.alive)
      .foreach(stateByUnit.remove)
    
    stateByUnit
      .values
      .filter(state => With.commander.readyForCommand(state.unit))
      .foreach(state => state.intent.behavior.execute(state.intent))
  }
  
  def getState(unit:FriendlyUnitInfo):ExecutionState = {
    if ( ! stateByUnit.contains(unit)) {
      stateByUnit.put(unit, new ExecutionState(unit))
    }
    stateByUnit(unit)
  }
}
