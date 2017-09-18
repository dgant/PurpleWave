package Micro.Actions.Combat.Tactics

import Micro.Actions.Action
import Micro.Actions.Combat.Decisionmaking.Engage
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Sally extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove && staticDefense(unit).nonEmpty
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val defense = staticDefense(unit)
    
    if (defense.nonEmpty) {
      unit.agent.toTravel = Some(defense.minBy(_.matchups.framesToLiveDiffused).pixelCenter)
      Engage.delegate(unit)
    }
  }
  
  def staticDefense(unit: FriendlyUnitInfo): Iterable[UnitInfo] = {
    val output = unit.matchups.allies.filter(staticDefense =>
      staticDefense.unitClass.topSpeed == 0               && //Buildings, Lurkers, and Siege Tanks
      staticDefense.unitClass.rawCanAttack                &&
      staticDefense.pixelDistanceFast(unit) < 32.0 * 15.0 &&
      staticDefense.matchups.dpfReceivingDiffused > 0)
    output
  }
}
