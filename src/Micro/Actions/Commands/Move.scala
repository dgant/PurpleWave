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
      && ! unit.unitClass.isWorker
      && unit.canAttack) {
      With.commander.attackMove(unit, pixelToMove)
    }
    else if (
      // If we have a ride
      unit.agent.ride.isDefined
      // and that ride can get us there faster
      && unit.framesToTravelTo(pixelToMove) >
        4 * unit.unitClass.groundDamageCooldown
        + unit.agent.ride.get.framesToTravelTo(unit.pixelCenter)
        + unit.agent.ride.get.framesToTravelPixels(unit.pixelDistanceCenter(pixelToMove))) {
      With.commander.rightClick(unit, unit.agent.ride.get)
    }
    else {
      With.commander.move(unit, pixelToMove)
    }
    unit.agent.directRide(pixelToMove)
  }
}
