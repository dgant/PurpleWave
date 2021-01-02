package Micro.Actions.Combat.Spells

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object WraithCloak extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    With.self.hasTech(Terran.WraithCloak)               &&
    unit.is(Terran.Wraith)                              &&
    ! unit.cloaked                                      &&
    unit.energy >= Terran.WraithCloak.energyCost + 10   &&
    With.framesSince(unit.lastFrameTakingDamage) < 24   &&
    ! With.grids.enemyDetection.isDetected(unit.tile)
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    With.commander.cloak(unit, Terran.WraithCloak)
  }
  
}
