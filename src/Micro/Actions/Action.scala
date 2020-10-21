package Micro.Actions

import Lifecycle.With
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

abstract class Action {
  
  val name: String = getClass.getSimpleName.replaceAllLiterally("$", "")

  protected def requiresReadiness: Boolean = true
  
  def allowed(unit: FriendlyUnitInfo): Boolean
  protected def perform(unit: FriendlyUnitInfo)
  
  final def consider(unit: FriendlyUnitInfo): Boolean = {
    act(unit, giveCredit = true)
  }
  
  final def delegate(unit: FriendlyUnitInfo): Boolean = {
    act(unit, giveCredit = false)
  }

  final def act(unit: FriendlyUnitInfo, giveCredit: Boolean): Boolean = {
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
   unit.unready
  }
}
