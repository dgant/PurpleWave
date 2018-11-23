package Micro.Actions.Protoss.Carrier

import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.CliffAvoid
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object CarrierOpenLeash extends Action {
  override def allowed(unit: FriendlyUnitInfo): Boolean = CliffAvoid.allowed(unit)
  override def perform(unit: FriendlyUnitInfo): Unit = CliffAvoid.perform(unit)
}
