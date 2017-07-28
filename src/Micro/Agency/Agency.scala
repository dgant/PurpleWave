package Micro.Agency

import Lifecycle.With
import Micro.Actions.Idle
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

class Agency {
  
  private val agents = new mutable.HashMap[FriendlyUnitInfo, Agent]
  private var finishedExecutingLastTime = true
  
  def states: Iterable[Agent] = agents.values
  
  def intend(intention: Intention) {
    getState(intention.unit).intent = intention
  }
  
  def getState(unit: FriendlyUnitInfo): Agent = {
    if ( ! agents.contains(unit)) {
      agents.put(unit, new Agent(unit))
    }
    agents(unit)
  }
  
  //////////////
  // Batching //
  //////////////
  
  val unitQueue = new mutable.Queue[Agent]
  
  def run() {
    
    if ( ! With.latency.isLastFrameOfTurn && finishedExecutingLastTime) return
  
    agents.keys.filterNot(_.alive).foreach(agents.remove)
    
    if (unitQueue.isEmpty) {
      unitQueue ++= agents.values.toVector.sortBy(_.lastFrame)
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
