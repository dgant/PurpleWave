package Micro.Actions.Protoss

import Information.Geography.Pathfinding.PathfindProfile
import Mathematics.Points.Pixel
import Micro.Actions.Action
import Micro.Actions.Combat.Targeting.Target
import Micro.Actions.Combat.Techniques.Avoid
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

object Paradrop extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.transport.isDefined && unit.isAny(Protoss.Reaver, Protoss.HighTemplar)

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val readyToDrop = unit.scarabCount > 0 || unit.energy >= 75
    val centroid = unit
      .squad.map(_.centroid)
      .orElse(unit.battle.filter(_ => unit.matchups.threats.nonEmpty).map(_.teamOf(unit).centroid))

    val home = centroid.getOrElse(unit.agent.origin).nearestWalkableTerrain

    var destination: Option[Pixel] = None
    if (readyToDrop) {
      if (unit.is(Protoss.Reaver)) {
        Target.consider(unit)
        destination = unit.agent.toAttack.map(_.pixelCenter)
      }
      destination = destination.orElse(Some(unit.agent.destination))
    }

    val profile = new PathfindProfile(unit.tileIncludingCenter)
    profile.end             = Some(home)
    profile.minimumLength   = Some(Math.min(unit.effectiveRangePixels.toInt / 32 - 1, ByOption.min(unit.matchups.threats.view.map(_.pixelRangeAgainst(unit).toInt / 32)).getOrElse(1)))
    profile.maximumLength   = Some(unit.effectiveRangePixels.toInt / 32)
    profile.flying          = false
    profile.allowGroundDist = false
    profile.costOccupancy   = 0.01f
    profile.costThreat      = 4
    profile.costRepulsion   = 2
    profile.repulsors       = Avoid.pathfindingRepulsion(unit)
    profile.unit            = Some(unit)
    val pathToDropSpot = profile.find
    if (pathToDropSpot.pathExists) {
      unit.agent.toTravel = Some(pathToDropSpot.end.pixelCenter)
      // TODO: Do we need to do more here?
    }
  }
}
