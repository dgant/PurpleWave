package Micro.Actions.Combat.Spells

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object GhostCloak extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    With.self.hasTech(Terran.GhostCloak)                                    &&
    unit.is(Terran.Ghost)                                                   &&
    ! unit.cloaked                                                          &&
    unit.energy >= Terran.GhostCloak.energyCost + 30                        &&
    unit.matchups.framesOfSafety < 12 + With.reaction.agencyAverage &&
    ! With.grids.enemyDetection.isSet(unit.tileIncludingCenter)
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    With.commander.cloak(unit, Terran.GhostCloak)
  }
  
}
