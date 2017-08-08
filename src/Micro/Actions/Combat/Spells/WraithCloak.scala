package Micro.Actions.Combat.Spells

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object WraithCloak extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.is(Terran.Wraith)                              &&
    ! unit.cloaked                                      &&
    unit.energy >= Terran.WraithCloak.energyCost + 10   &&
    unit.damageInLastSecond > 0                         &&
    ! With.grids.enemyDetection.get(unit.tileIncludingCenter)
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    With.commander.cloak(unit, Terran.WraithCloak)
  }
  
}
