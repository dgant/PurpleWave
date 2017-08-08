package Micro.Actions.Combat.Maneuvering

import Micro.Actions.Action
import Micro.Actions.Combat.Attacking.Target
import Micro.Actions.Commands.{Attack, Travel}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object AttackAndReposition extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove && unit.ranged
  }
  
  override def perform(unit: FriendlyUnitInfo) {
  
    Target.delegate(unit)
    Attack.delegate(unit)
    if (unit.readyForMicro && (unit.matchups.targets.isEmpty || unit.matchups.targetsInRange.nonEmpty)) {
      Avoid.delegate(unit)
    }
    if (unit.matchups.threats.isEmpty) {
      unit.agent.toTravel = unit.agent.toAttack.map(_.pixelCenter)
      Travel.delegate(unit)
    }
  }
}
