package Micro.Actions.Commands

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Attack extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.canFight &&
    unit.agent.toAttack.isDefined
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    if (unit.is(Zerg.Lurker) && ! unit.burrowed) {
      unit.agent.toTravel = Some(unit.agent.toAttack.get.pixelCenter)
      Move.delegate(unit)
    }
    
    if (unit.agent.toAttack.get.is(Protoss.Interceptor)
    || (unit.agent.toAttack.get.is(Protoss.Carrier) && ! unit.inRangeToAttack(unit.agent.toAttack.get))) {
      unit.agent.toTravel = unit.agent.toAttack.map(_.pixelCenter)
      AttackMove.delegate(unit)
    }
    
    if (unit.unitClass.accelerationFrames <= 1 && unit.matchups.targetsInRange.forall(unit.agent.toAttack.contains)) {
      // TODO: Try enabling this; have never actually tested this
      //With.commander.hold(unit)
    }
    
    With.commander.attack(unit, unit.agent.toAttack.get)
  }
}
