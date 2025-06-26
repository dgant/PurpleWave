package Micro.Agency

import Lifecycle.With
import Mathematics.Maff
import Micro.Actions.ActionPerformance
import Performance.Tasks.TimedTask
import Performance.Timer
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Utilities.?

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class Agency extends TimedTask {

  withAlwaysSafe(true)

  var lastQueueCompletion = 0
  var agentIndex          = 0
  val agents              = new ArrayBuffer[Agent]
  val cycleLengths        = new mutable.Queue[Int]
  val actionPerformance   = new mutable.HashMap[String, ActionPerformance]()

  override def isComplete: Boolean = agents.isEmpty

  override def onRun(budgetMs: Long): Unit = {

    val timer = new Timer(budgetMs)

    if (agentIndex >= agents.length) {
      cycleLengths.enqueue(With.framesSince(lastQueueCompletion))
      while (cycleLengths.sum > With.reaction.runtimeQueueDuration) { cycleLengths.dequeue() }
      lastQueueCompletion = With.frame

      With.coordinator.onAgentCycle()

      agentIndex = 0
      agents.clear()
      agents ++= With.units.ours
        .filter(u =>
          u.unitClass.orderable
          && ?(
            u.unitClass.isBuilding,
            ! u.complete || ! u.isAny(Terran.SupplyDepot, Protoss.Pylon),
            u.complete)
          || u.isAny(Zerg.Egg, Zerg.LurkerEgg, Zerg.Cocoon))
        .map(_.agent)

      Maff.sortStablyInPlaceBy(agents, 0, agents.length)(a =>
        Maff.or1(1e5, a.unit.unitClass.isTransport)
        - ?(a.unit.unitClass.isWarrior, a.unit.matchups.pixelsToThreatRange.getOrElse(0.0), 0.0))
    }

    while (agentIndex < agents.length && timer.greenLight) {
      val agent       =   agents(agentIndex)
      val unit        =   agent.unit
      agentIndex      +=  1
      unit.sleepUntil =   Math.max(unit.sleepUntil, AttackDelay.nextSafeOrderFrame(unit))

      if (unit.alive && unit.ready) {
        val timeBefore = With.performance.frameElapsedMs
        agent.execute()
        agent.toAttack.foreach(_.addTargeter(unit))

        if (With.configuration.enablePerformancePauses && timer.redLight && With.performance.frameBrokeLimit) {
          val timeAfter = With.performance.frameElapsedMs
          val timeDelta = timeAfter - timeBefore
          With.logger.performance(f"${unit} crossed ${With.configuration.frameLimitMs}ms: ${timeDelta}ms on ${if (agent.actions.isEmpty) agent.lastAction.get else agent.actions.mkString(", ")}")
        }

        unit.hysteresis.update() // Update hysteresis immediately so combat sim has access to the latest value
      }
    }
  }
}
