package Micro.Actions.Combat.Specialized

import Micro.Actions.Action
import Micro.Actions.Combat.Decisionmaking.Disengage
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Recover extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    Seq(
      unit.is(Protoss.Carrier)      && unit.interceptorCount < 4,
      unit.is(Protoss.Reaver)       && unit.scarabCount == 0,
      unit.is(Protoss.HighTemplar)  && unit.energy < 60)
    .contains(true)
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    Disengage.delegate(unit)
  }
}
