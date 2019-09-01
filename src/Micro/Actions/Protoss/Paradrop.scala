package Micro.Actions.Protoss

import Information.Geography.Pathfinding.PathfindProfile
import Lifecycle.{Manners, With}
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.FollowPath
import Micro.Actions.Combat.Targeting.Target
import Micro.Actions.Combat.Techniques.Avoid
import Micro.Actions.Commands.Attack
import Planning.UnitMatchers.UnitMatchRecruitableForCombat
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

object Paradrop extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.transport.isDefined && unit.isAny(Protoss.Reaver, Protoss.HighTemplar)

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val reaverCanFight  = unit.scarabCount > 0 && (unit.agent.shouldEngage || unit.matchups.threats.forall(_.pixelRangeAgainst(unit) <= unit.effectiveRangePixels))
    val templarCanFight = unit.energy >= 75
    val readyToDrop     = reaverCanFight || templarCanFight

    // If we're able to fight, pick a target
    if (readyToDrop) {
      if (unit.is(Protoss.Reaver)) {
        Target.consider(unit)
      }
    }

    // If we can drop out and attack now, do so
    if (unit.agent.toAttack.exists(unit.inRangeToAttack)) {
      val shouldDrop = unit.matchups.framesOfSafety > unit.cooldownLeft || (unit.agent.shouldEngage && unit.matchups.threatsInRange.forall(u => u.pixelRangeAgainst(unit) + 16 >= u.effectiveRangePixels))
      if (shouldDrop) {
        Attack.delegate(unit)
        return
      }
    }
    val target = unit.agent.toAttack

    // If we're attacking, drop in firing position
    // Otherwise, flee
    def getCentroid(units: Iterable[UnitInfo]): Option[Pixel] = {
      val eligibleUnits = units.view.filter(u => unit.is(UnitMatchRecruitableForCombat) && ! u.isAny(Protoss.Shuttle, Protoss.Reaver, Protoss.HighTemplar))
      if (eligibleUnits.size < 3) None else Some(PurpleMath.centroid(eligibleUnits.map(_.pixelCenter)))
    }
    val destinationAir =
      unit.agent.toAttack.map(_.pixelCenter)
        .orElse(unit.squad.flatMap(squad => getCentroid(squad.units.view)))
        .orElse(getCentroid(unit.matchups.allies))
        .getOrElse(if (unit.agent.shouldEngage) unit.agent.destination else unit.agent.origin)
    val destination = destinationAir.nearestWalkableTerrain
    unit.agent.toTravel = Some(destination.pixelCenter)

    val profile = new PathfindProfile(unit.tileIncludingCenter)
    profile.end                 = Some(destination)
    profile.minimumLength       = Some(Math.min(unit.effectiveRangePixels.toInt / 32 - 1, ByOption.min(unit.matchups.threats.view.map(_.pixelRangeAgainst(unit).toInt / 32)).getOrElse(1)))
    profile.maximumLength       = Some(unit.effectiveRangePixels.toInt / 32)
    profile.canCrossUnwalkable  = false
    profile.allowGroundDist     = false
    profile.costOccupancy       = 0.01f
    profile.costThreat          = 3
    profile.costRepulsion       = if (target.isDefined) 0.5f else 1.5f
    profile.repulsors           = Avoid.pathfindingRepulsion(unit)
    profile.unit                = Some(unit)
    val path = profile.find
    if (path.pathExists) {
      unit.agent.toTravel = Some(path.end.pixelCenter)
      new FollowPath(path).delegate(unit)
    } else if (With.configuration.debugging()) {
      Manners.chat(f"Failed to path $unit to $destination")
      Manners.chat(f"Targeting $target")
    }
  }
}
