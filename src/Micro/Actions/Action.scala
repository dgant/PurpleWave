package Micro.Actions

import Lifecycle.With
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

abstract class Action {
  
  val name: String = getClass.getSimpleName.replaceAllLiterally("$", "")
  
  protected def requiresReadiness: Boolean = true
  
  def allowed(unit: FriendlyUnitInfo): Boolean
  protected def perform(unit: FriendlyUnitInfo)
  
  final def consider(unit: FriendlyUnitInfo, giveCredit: Boolean = true): Boolean = {
    if (( ! requiresReadiness || unit.ready) && allowed(unit)) {
      val previousCredit = unit.agent.lastAction
      if (giveCredit) unit.agent.lastAction = Some(this)
      if (With.configuration.debugging) {
        unit.agent.actionsPerformed += this
      }
      perform(unit)
      if (unit.ready) {
        unit.agent.lastAction = previousCredit
      }
    }
    ! unit.ready
  }
  
  final def delegate(unit: FriendlyUnitInfo): Boolean = {
    consider(unit, giveCredit = false)
  }
}
