package Micro.Agency

import Lifecycle.With
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

class Agency {
  
  private var finishedExecutingLastTime = true
  
  //////////////
  // Batching //
  //////////////
  
  val agentQueue            = new mutable.Queue[Agent]
  var lastQueueCompletion   = 0
  val runtimes              = new mutable.Queue[Int]
  
  def run() {
  
    if ( ! With.latency.isLastFrameOfTurn && finishedExecutingLastTime) return
    
    if (agentQueue.isEmpty) {
      runtimes.enqueue(With.framesSince(lastQueueCompletion))
      while (runtimes.sum > With.reaction.runtimeQueueDuration) { runtimes.dequeue() }
      lastQueueCompletion = With.frame

      With.coordinator.runPerAgentCycle()
      With.squads.updateGoals()
      agentQueue ++= With.units.ours.view
        .filter(validAgent)
        .map(_.agent)
        .toVector
        .sortBy(_.unit.frameDiscovered) // Start with a stable order
        .sortBy(_.unit.matchups.framesOfSafety) // Units in trouble get first dibs on things
        .sortBy(_.unit.unitClass.isTransport) // Make transports go after their passengers so they know what passengers want
    }
    
    var doContinue = true
    while (doContinue && agentQueue.nonEmpty) {
      doContinue = doContinue && With.performance.continueRunning
      val agent = agentQueue.dequeue()
      if (agent.unit.unitClass.orderable && agent.unit.alive && agent.unit.ready) {
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
