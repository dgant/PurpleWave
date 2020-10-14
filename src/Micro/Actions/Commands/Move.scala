package Micro.Actions.Commands

import Information.Geography.Pathfinding.PathfindProfile
import Lifecycle.With
import Micro.Actions.Action
import Micro.Coordination.Pathing.MicroPathing
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Strategery.MapGroups

object Move extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove &&
    unit.agent.toTravel.isDefined
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    val pixelToMove = unit.agent.toTravel.get

    // Do a pathfinding move
    if ( ! With.performance.danger && MapGroups.needCustomPathing.exists(_.matches)) {
      lazy val tileToMove = pixelToMove.nearestWalkableTerrain
      if ( ! unit.flying
        && unit.agent.path.isEmpty
        && unit.pixelDistanceTravelling(tileToMove) > 128 + unit.pixelDistanceCenter(pixelToMove)) {
         val profile = new PathfindProfile(unit.tileIncludingCenter)
            profile.end                 = Some(tileToMove)
            profile.lengthMaximum       = Some(24)
            profile.threatMaximum       = Some(0)
            profile.canCrossUnwalkable  = false
            profile.allowGroundDist     = true
            profile.unit = Some(unit)
        val path = profile.find
        MicroPathing.tryMovingAlongTilePath(unit, path)
      }
    }
    
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
