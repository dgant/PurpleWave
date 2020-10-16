package Micro.Actions.Commands

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Attack extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.canFight &&
    unit.agent.toAttack.isDefined
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    val target = unit.agent.toAttack.get

    val dropship = unit.transport.find(_.isAny(Terran.Dropship, Protoss.Shuttle, Zerg.Overlord))
    val delay = unit.cooldownMaxAirGround
    if (dropship.isDefined
      && Math.min(unit.pixelDistanceEdge(target), unit.pixelDistanceEdge(target.projectFrames(delay)))
      <= unit.pixelRangeAgainst(target)
        + With.reaction.agencyAverage
        + delay * unit.topSpeed) {
      With.commander.unload(dropship.get, unit)
      return
    }

    if (unit.is(Zerg.Lurker) && ! unit.burrowed) {
      unit.agent.toStep = Some(target.pixelCenter)
      Move.delegate(unit)
    }
    
    if (target.is(Protoss.Interceptor)) {
      unit.agent.toStep = Some(target.pixelCenter)
      With.commander.attackMove(unit)
    }
    
    if (unit.unitClass.accelerationFrames <= 1 && unit.matchups.targetsInRange.forall(unit.agent.toAttack.contains)) {
      With.commander.hold(unit)
    } else {
      With.commander.attack(unit)
    }

  }
}
