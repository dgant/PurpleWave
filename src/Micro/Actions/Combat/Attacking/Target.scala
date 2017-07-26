package Micro.Actions.Combat.Attacking

import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Target extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.action.canFight          &&
    unit.action.toAttack.isEmpty  &&
    unit.canAttackThisSecond      &&
    unit.matchups.targets.nonEmpty
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    lazy val canPillage = unit.matchups.threats.isEmpty
    lazy val canPursue  = unit.action.canPursue
    lazy val arrived    = unit.action.toTravel.forall(_.zone == unit.pixelCenter.zone)
    
    if (canPillage || canPursue || arrived) {
      TargetAnything.delegate(unit)
    }
    else {
      TargetRelevant.delegate(unit)
    }
  }
}
