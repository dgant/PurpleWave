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
    if (unit.is(Zerg.Lurker)
      && ! unit.burrowed
      && unit.agent.toAttack.isDefined) {
      
      val targetPixel = unit.agent.toAttack.get.pixelCenter
      unit.agent.toTravel = Some(targetPixel)
      if (unit.pixelDistanceEdge(unit.agent.toAttack.get) < 64) {
        With.commander.burrow(unit)
      }
      Move.delegate(unit)
    }
    With.commander.attack(unit, unit.agent.toAttack.get)
  }
}
