package Micro.Actions.Combat.Spells

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object WraithUncloak extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.is(Terran.Wraith)        &&
    unit.cloaked                  &&
    unit.matchups.threats.isEmpty &&
    With.framesSince(unit.agent.lastCloak) > 24 * 4
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    With.commander.decloak(unit, Terran.WraithCloak)
  }
}
