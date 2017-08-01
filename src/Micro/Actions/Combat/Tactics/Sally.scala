package Micro.Actions.Combat.Tactics

import Micro.Actions.Action
import Micro.Actions.Combat.Decisionmaking.Engage
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Sally extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove &&
    unit.matchups.allies.exists(staticDefense =>
      staticDefense.unitClass.topSpeed == 0   && //Buildings, Lurkers, and Siege Tanks
      staticDefense.unitClass.rawCanAttack    &&
      staticDefense.matchups.dpfReceivingDiffused > 0)
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    Engage.delegate(unit)
  }
}
