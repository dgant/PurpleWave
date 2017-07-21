package Micro.Actions.Combat.Specialized

import Micro.Actions.Action
import Micro.Actions.Commands.Travel
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object CarrierRetreat extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.is(Protoss.Carrier)
  }
  
  // Carriers can't afford to be fancy in their retreating.
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    unit.action.toTravel = Some(unit.action.origin)
    Travel.delegate(unit)
  }
}
