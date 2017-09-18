package Micro.Actions

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

abstract class Action {
  
  val name: String = getClass.getSimpleName.replaceAllLiterally("$", "")
  
  protected def requiresReadiness: Boolean = true
  
  def allowed(unit: FriendlyUnitInfo): Boolean
  protected def perform(unit: FriendlyUnitInfo)
  
  final def consider(unit: FriendlyUnitInfo, giveCredit: Boolean = true) {
    if (( ! requiresReadiness || unit.readyForMicro) && allowed(unit)) {
      if (giveCredit) unit.agent.lastAction = Some(this)
      perform(unit)
    }
  }
  
  final def delegate(unit: FriendlyUnitInfo) {
    consider(unit, giveCredit = false)
  }
}
