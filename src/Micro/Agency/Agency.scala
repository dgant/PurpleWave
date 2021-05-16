package Micro.Agency

import Lifecycle.With
import Performance.Tasks.TimedTask
import Performance.Timer

import scala.collection.mutable

class Agency extends TimedTask {

  withAlwaysSafe(true)
  
  val agentQueue          = new mutable.Queue[Agent]
  val cycleLengths        = new mutable.Queue[Int]
  var lastQueueCompletion = 0

  override def isComplete: Boolean = agentQueue.isEmpty

  override def onRun(budgetMs: Long) {

    val timer = new Timer(budgetMs)

    if (agentQueue.isEmpty) {
      cycleLengths.enqueue(With.framesSince(lastQueueCompletion))
      while (cycleLengths.sum > With.reaction.runtimeQueueDuration) { cycleLengths.dequeue() }
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

    while (agentQueue.nonEmpty && timer.ongoing) {
      val agent = agentQueue.dequeue()
      val unit = agent.unit
      unit.sleepUntil(Math.max(unit.nextOrderFrame.getOrElse(0), AttackDelay.nextSafeOrderFrame(unit)))
      if (unit.unitClass.orderable && agent.unit.alive && agent.unit.ready) {
        val timeBefore = With.performance.frameMs
        agent.execute()
        if (With.performance.violatedLimit && With.configuration.enablePerformancePauses) {
          val timeAfter = With.performance.frameMs
          val timeDelta = timeAfter - timeBefore
          With.logger.warn(f"Microing ${unit.unitClass} crossed the ${With.configuration.frameLimitMs} threshold by taking ${timeDelta}ms considering ${if (agent.actionsPerformed.isEmpty) agent.lastAction.get else agent.actionsPerformed.map(_.name).mkString(", ")}")
        }
      }
    }
  }
}
