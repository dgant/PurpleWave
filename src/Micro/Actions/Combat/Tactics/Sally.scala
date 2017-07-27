package Micro.Actions.Combat.Tactics

import Micro.Actions.Action
import Micro.Actions.Combat.Decisionmaking.Engage
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Sally extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMoveThisFrame &&
    unit.matchups.allies.exists(staticDefense =>
      staticDefense.unitClass.isBuilding  &&
      staticDefense.unitClass.canAttack   &&
      staticDefense.matchups.dpfReceivingDiffused > 0)
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    Engage.delegate(unit)
  }
}
