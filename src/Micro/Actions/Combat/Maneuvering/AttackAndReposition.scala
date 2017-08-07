package Micro.Actions.Combat.Maneuvering

import Micro.Actions.Action
import Micro.Actions.Combat.Attacking.Target
import Micro.Actions.Commands.Attack
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object AttackAndReposition extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove && unit.ranged
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    
    if (unit.readyForAttackOrder) {
      Target.delegate(unit)
      Attack.delegate(unit)
    }
    
    if (unit.readyForMicro && (unit.matchups.targets.isEmpty || unit.matchups.targetsInRange.nonEmpty)) {
      Avoid.delegate(unit)
    }
  }
}
