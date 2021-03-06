package Micro.Actions.Combat.Maneuvering

import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Smuggle extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove && unit.loadedUnits.nonEmpty
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    if (unit.matchups.threats.exists(_.framesBeforeAttacking(unit) < 24)) {
      Retreat.delegate(unit)
    }
    Commander.move(unit)
  }
}
