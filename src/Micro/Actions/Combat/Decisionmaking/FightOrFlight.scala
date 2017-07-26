package Micro.Actions.Combat.Decisionmaking

import Mathematics.PurpleMath
import Micro.Actions.Action
import Planning.Yolo
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object FightOrFlight extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.action.canFight
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    if (Yolo.active) {
      Engage.consider(unit)
    }
  
    lazy val doomed         = unit.matchups.doomed
    lazy val matchups       = unit.matchups.inFrames(24)
    lazy val groupDesire    = unit.battle.map(_.localAttackDesire).getOrElse(1.0)
    lazy val personalDesire = PurpleMath.nanToInfinity(matchups.vpfDealingDiffused / matchups.vpfReceivingDiffused)
    lazy val totalDesire    = groupDesire * personalDesire
    
    if (doomed) {
      Engage.consider(unit)
    }
    if (totalDesire < 1.0) {
      Disengage.consider(unit)
    }
    Engage.consider(unit)
  }
}
