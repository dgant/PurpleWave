package Micro.Actions.Combat.Tactics

import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Form extends Action {
  
  override def allowed(unit: FriendlyUnitInfo) = unit.agent.toForm.isDefined
  
  override def perform(unit: FriendlyUnitInfo) {
    Bunk.delegate(unit)
  }
}
