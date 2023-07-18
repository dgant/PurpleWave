package Micro.Agency

import Lifecycle.With
import Micro.Actions.ActionPerformance
import Performance.Tasks.TimedTask
import Performance.Timer

import scala.collection.mutable

class Agency extends TimedTask {

  withAlwaysSafe(true)

  var lastQueueCompletion = 0
  val agentQueue          = new mutable.Queue[Agent]
  val cycleLengths        = new mutable.Queue[Int]
  val actionPerformance   = new mutable.HashMap[String, ActionPerformance]()

  override def isComplete: Boolean = agentQueue.isEmpty

  override def onRun(budgetMs: Long): Unit = {

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
        .sortBy(- _.unit.matchups.pixelsEntangled) // Units in trouble get first dibs on things
        .sortBy(_.unit.unitClass.isTransport) // Make transports go after their passengers so they know what passengers want
    }

    while (agentQueue.nonEmpty && timer.greenLight) {
      val agent = agentQueue.dequeue()
      val unit = agent.unit
      unit.sleepUntil = Math.max(unit.sleepUntil, AttackDelay.nextSafeOrderFrame(unit))
      if (unit.unitClass.orderable && agent.unit.alive && agent.unit.ready) {
        val timeBefore = With.performance.frameElapsedMs
        agent.execute()
        agent.toAttack.foreach(_.addTargeter(unit))
        if (With.configuration.enablePerformancePauses && timer.redLight && With.performance.frameBrokeLimit) {
          val timeAfter = With.performance.frameElapsedMs
          val timeDelta = timeAfter - timeBefore
          With.logger.performance(f"${unit.unitClass} broke ${With.configuration.frameLimitMs}ms: ${timeDelta}ms on ${if (agent.actions.isEmpty) agent.lastAction.get else agent.actions.mkString(", ")}")
        }
      }
    }
  }
}
