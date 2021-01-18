package Micro.Agency

import Lifecycle.With

import scala.collection.mutable

class Agency {
  
  //////////////
  // Batching //
  //////////////
  
  val agentQueue          = new mutable.Queue[Agent]
  val runtimes            = new mutable.Queue[Int]
  var lastQueueCompletion = 0
  
  def run() {
    if (agentQueue.isEmpty) {
      runtimes.enqueue(With.framesSince(lastQueueCompletion))
      while (runtimes.sum > With.reaction.runtimeQueueDuration) { runtimes.dequeue() }
      lastQueueCompletion = With.frame

      With.coordinator.onAgentCycle()
      agentQueue ++= With.units.ours.view
        .filter(u => u.alive && (u.complete || u.unitClass.isBuilding) && u.unitClass.orderable)
        .map(_.agent)
        .toVector
        .sortBy(_.unit.frameDiscovered) // Start with a stable order
        .sortBy(- _.unit.matchups.pixelsOfEntanglement) // Units in trouble get first dibs on things
        .sortBy(_.unit.unitClass.isTransport) // Make transports go after their passengers so they know what passengers want
    }

    while (agentQueue.nonEmpty && With.performance.continueRunning) {
      val agent = agentQueue.dequeue()
      if (agent.unit.unitClass.orderable && agent.unit.alive && agent.unit.ready) {
        val timeBefore = With.performance.frameMs
        agent.execute()
        if (With.performance.violatedLimit && With.configuration.enablePerformancePauses) {
          val timeAfter = With.performance.frameMs
          val timeDelta = timeAfter - timeBefore
          With.logger.warn(
            "Microing "
            + agent.unit.unitClass
            + " crossed the "
            + With.configuration.frameLimitMs
            + "ms threshold by taking "
            + timeDelta
            + "ms considering "
            + agent.actionsPerformed.map(_.toString).mkString(", "))
        }
      }
    }
  }
}
