package Micro.Actions

import Debugging.ToString
import Lifecycle.With
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

abstract class Action {
  
  val name: String = ToString(this)

  protected def requiresReadiness: Boolean = true
  
  def allowed(unit: FriendlyUnitInfo): Boolean = true
  protected def perform(unit: FriendlyUnitInfo): Unit
  
  final def apply(unit: FriendlyUnitInfo): Boolean = {
    act(unit, giveCredit = true)
  }
  
  final def delegate(unit: FriendlyUnitInfo): Boolean = {
    act(unit, giveCredit = false)
  }

  final def act(unit: FriendlyUnitInfo, giveCredit: Boolean): Boolean = {
    val nanosBefore = System.nanoTime()
    if (( ! requiresReadiness || unit.ready) && allowed(unit)) {
      val previousCredit = unit.agent.lastAction
      if (giveCredit) unit.agent.act(name)
      if (With.configuration.debugging) {
        unit.agent.actionsPerformed += this
      }
      perform(unit)
      if (unit.ready) {
        previousCredit.foreach(unit.agent.act)
      }
    }
    With.agents.actionPerformance(this) = With.agents.actionPerformance.getOrElse(this, new ActionPerformance)
    val performance = With.agents.actionPerformance(this)
    val nanosAfter = System.nanoTime()
    performance.durationNanos += Math.max(0, nanosAfter - nanosBefore)
    performance.invocations += 1
    unit.unready
  }
}
