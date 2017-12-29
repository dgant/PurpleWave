package Micro.Actions.Combat.Tactics

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Unbunk extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.transport.exists(_.is(Terran.Bunker))          &&
    unit.transport.get.matchups.targetsInRange.isEmpty  &&
    unit.agent.toForm.forall(_.pixelDistanceFast(unit.pixelCenter) > 32.0 * 7.0)
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    With.commander.unload(unit.transport.get, unit)
  }
}
