package Micro.Actions.Combat.Spells

import Lifecycle.With
import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object WraithUncloak extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.is(Terran.Wraith)
    && unit.cloaked
    && ! unit.matchups.enemies.exists(_.unitClass.attacksAir)
    && With.framesSince(unit.agent.lastCloak) > 24 * 10
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    Commander.decloak(unit, Terran.GhostCloak)
  }
}
