package Micro.Agency

import Lifecycle.With
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

class Agency {
  
  private val agents = new mutable.HashMap[FriendlyUnitInfo, Agent]
  private var finishedExecutingLastTime = true
  
  def all: Iterable[Agent] = agents.values
  
  def getState(unit: FriendlyUnitInfo): Agent = {
    if ( ! agents.contains(unit)) {
      agents.put(unit, new Agent(unit))
    }
    agents(unit)
  }
  
  //////////////
  // Batching //
  //////////////
  
  val agentQueue            = new mutable.Queue[Agent]
  var lastQueueCompletion   = 0
  val runtimes              = new mutable.Queue[Int]
  
  def run() {
  
    if ( ! With.latency.isLastFrameOfTurn && finishedExecutingLastTime) return
  
    agents.keys.filterNot(validAgent).foreach(agents.remove)
    
    if (agentQueue.isEmpty) {
      runtimes.enqueue(With.framesSince(lastQueueCompletion))
      while (runtimes.sum > 24 * 10) runtimes.dequeue()
      lastQueueCompletion = With.frame
      
      // Make sure our orderable units all have agents
      With.units.ours.filter(validAgent).foreach(getState)
      agentQueue ++= agents.values.toVector.sortBy(_.lastFrame)
    }
    
    var doContinue = true
    while (doContinue && agentQueue.nonEmpty) {
      doContinue = doContinue && With.performance.continueRunning
      val agent = agentQueue.dequeue()
      agent.lastFrame = With.frame
      
      if (agent.unit.unitClass.orderable && agent.unit.alive && agent.unit.readyForMicro) {
        agent.execute()
      }
    }
  
    finishedExecutingLastTime = agentQueue.isEmpty
  }
  
  private def validAgent(unit: FriendlyUnitInfo): Boolean = {
    unit.alive                                    &&
    (unit.complete || unit.unitClass.isBuilding)  &&
    (unit.unitClass.orderable || unit.is(Protoss.Interceptor))
  }
}
