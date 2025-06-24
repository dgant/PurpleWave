package Micro.Actions.Combat.Spells

import Lifecycle.With
import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object WraithCloak extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    Terran.WraithCloak()
    && Terran.Wraith(unit)
    && ! unit.cloaked
    && unit.confidence11 < 0.9
    && unit.matchups.pixelsToThreatRange.exists(_ < 32)
    && unit.energy >= Terran.WraithCloak.energyCost + 20
    && ! With.grids.enemyDetection.inRange(unit.tile))

  protected def perform(unit: FriendlyUnitInfo): Unit = {
    Commander.cloak(unit, Terran.WraithCloak)
  }
}
