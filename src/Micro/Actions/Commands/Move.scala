package Micro.Actions.Commands

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Move extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove &&
    unit.agent.toTravel.isDefined
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    val pixelToMove = unit.agent.toTravel.get
    
    if (unit.agent.shouldEngage
      && With.reaction.agencyAverage > 12
      && ! unit.unitClass.isWorker) {
      With.commander.attackMove(unit, pixelToMove)
    }
    else {
      With.commander.move(unit, pixelToMove)
    }
  }
}
