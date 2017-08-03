package Micro.Actions.Combat.Tactics

import Micro.Actions.Action
import Micro.Actions.Combat.Decisionmaking.Engage
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Sally extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val staticDefense = unit.matchups.allies.filter(staticDefense =>
      staticDefense.unitClass.topSpeed == 0               && //Buildings, Lurkers, and Siege Tanks
      staticDefense.unitClass.rawCanAttack                &&
      staticDefense.pixelDistanceFast(unit) < 32.0 * 15.0 &&
      staticDefense.matchups.dpfReceivingDiffused > 0)
    
    if (staticDefense.nonEmpty) {
      unit.agent.toTravel = Some(staticDefense.minBy(_.matchups.framesToLiveDiffused).pixelCenter)
      Engage.delegate(unit)
    }
  }
}
