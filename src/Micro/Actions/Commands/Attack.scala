package Micro.Actions.Commands

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Attack extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.canFight &&
    unit.agent.toAttack.isDefined
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    if (unit.is(Zerg.Lurker) && ! unit.burrowed) {
      unit.agent.toTravel = unit.agent.toAttack.map(_.pixelCenter)
      Move.delegate(unit)
    }
    With.commander.attack(unit, unit.agent.toAttack.get)
  }
}
