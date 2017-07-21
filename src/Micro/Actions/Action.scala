package Micro.Actions

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

abstract class Action {
  
  val name: String = getClass.getSimpleName.replaceAllLiterally("$", "")
  
  protected def allowed(unit: FriendlyUnitInfo): Boolean
  protected def perform(unit: FriendlyUnitInfo)
  
  final def consider(unit: FriendlyUnitInfo, giveCredit: Boolean = true) {
    if (unit.readyForMicro && allowed(unit)) {
      if (giveCredit) unit.action.lastAction = Some(this)
      perform(unit)
    }
  }
  
  final def delegate(unit: FriendlyUnitInfo) {
    consider(unit, giveCredit = false)
  }
}
