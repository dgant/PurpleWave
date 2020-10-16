package Micro.Actions.Commands

import Information.Geography.Pathfinding.PathfindProfile
import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Actions.Action
import Micro.Coordination.Pathing.MicroPathing
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Strategery.MapGroups

object Move extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove &&
    unit.agent.toTravel.isDefined
  }

  def doMove(unit: FriendlyUnitInfo): Unit = {
    if (unit.agent.toTravel.isEmpty) return

    def toTravel: Pixel = unit.agent.toTravel.get

    // Do a pathfinding move
    if ( ! With.performance.danger && MapGroups.needCustomPathing.exists(_.matches)) {
      lazy val tileToMove = toTravel.nearestWalkableTerrain
      if ( ! unit.flying
        // TODO: Don't re-path!
        //&& unit.agent.path.isEmpty
        && unit.pixelDistanceTravelling(tileToMove) > 128 + unit.pixelDistanceCenter(toTravel)) {
         val profile = new PathfindProfile(unit.tileIncludingCenter)
            profile.end                 = Some(tileToMove)
            profile.lengthMaximum       = Some(24)
            profile.threatMaximum       = Some(0)
            profile.canCrossUnwalkable  = false
            profile.allowGroundDist     = true
            profile.unit = Some(unit)
        val path = profile.find
        unit.agent.toTravel = MicroPathing.getWaypointAlongTilePath(path).orElse(unit.agent.toTravel)
      }
    }

    // When bot is slowing down, use attack-move
    if (unit.agent.shouldEngage
      && With.reaction.agencyAverage > 12
      && ! unit.unitClass.isWorker
      && unit.canAttack) {
      With.commander.attackMove(unit)
    }

    else if (
      // If we have a ride
      unit.agent.ride.isDefined
      // and that ride can get us there faster
      && unit.framesToTravelTo(toTravel) >
        4 * unit.unitClass.groundDamageCooldown
        + unit.agent.ride.get.framesToTravelTo(unit.pixelCenter)
        + unit.agent.ride.get.framesToTravelPixels(unit.pixelDistanceCenter(toTravel))) {
      With.commander.rightClick(unit, unit.agent.ride.get)
    }
    else {
      With.commander.move(unit)
    }
    unit.agent.directRide(toTravel)
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    doMove(unit)
  }
}
