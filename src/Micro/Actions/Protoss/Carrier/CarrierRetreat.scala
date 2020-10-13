package Micro.Actions.Protoss.Carrier

import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object CarrierRetreat extends Action {
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.is(Protoss.Carrier) && Retreat.allowed(unit)
  override def perform(unit: FriendlyUnitInfo): Unit = Retreat.delegate(unit)
}