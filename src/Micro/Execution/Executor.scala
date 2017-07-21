package Micro.Execution

import Lifecycle.With
import Micro.Actions.Idle
import Micro.Intent.Intention
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

class Executor {
  
  private val stateByUnit = new mutable.HashMap[FriendlyUnitInfo, ActionState]
  private var finishedExecutingLastTime = true
  
  def states: Iterable[ActionState] = stateByUnit.values
  
  def intend(intention: Intention) {
    getState(intention.unit).intent = intention
  }
  
  def getState(unit: FriendlyUnitInfo): ActionState = {
    if ( ! stateByUnit.contains(unit)) {
      stateByUnit.put(unit, new ActionState(unit))
    }
    stateByUnit(unit)
  }
  
  //////////////
  // Batching //
  //////////////
  
  val unitQueue = new mutable.Queue[ActionState]
  
  def run() {
    
    if ( ! With.latency.isLastFrameOfTurn && finishedExecutingLastTime) return
  
    stateByUnit.keys.filterNot(_.alive).foreach(stateByUnit.remove)
    
    if (unitQueue.isEmpty) {
      unitQueue ++= stateByUnit.values.toVector.sortBy(_.lastFrame)
    }
    
    var doContinue = true
    while (doContinue && unitQueue.nonEmpty) {
      doContinue = doContinue && With.performance.continueRunning
      val nextState = unitQueue.dequeue()
      nextState.lastFrame = With.frame
      
      if (nextState.unit.unitClass.orderable
        && ! nextState.unit.is(Protoss.PhotonCannon)) /* Hack fix for CIG -- we were cancelling our Photon Cannon attacks. */ {
        Idle.consider(nextState.unit)
      }
    }
  
    finishedExecutingLastTime = unitQueue.isEmpty
  }
}
