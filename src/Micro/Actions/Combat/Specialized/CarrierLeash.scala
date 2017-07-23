package Micro.Actions.Combat.Specialized

import Micro.Actions.Action
import Micro.Actions.Combat.Decisionmaking.Disengage
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, Orders}

object CarrierLeash extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.interceptors.exists(_.order == Orders.InterceptorAttack) &&
    unit.matchups.targets.exists(_.pixelDistanceFast(unit) < 32.0 * 8.0)
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    Disengage.consider(unit)
  }
}
