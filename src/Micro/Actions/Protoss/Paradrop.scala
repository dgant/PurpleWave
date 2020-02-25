package Micro.Actions.Protoss

import Information.Geography.Pathfinding.PathfindProfile
import Information.Geography.Pathfinding.Types.NoPath
import Lifecycle.{Manners, With}
import Mathematics.PurpleMath
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Traverse
import Micro.Actions.Combat.Targeting.Target
import Micro.Actions.Combat.Techniques.Avoid
import Micro.Actions.Commands.Attack
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

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
    if (unit.agent.toAttack.exists(target => unit.pixelDistanceEdge(target) <= unit.pixelRangeAgainst(target) + unit.cooldownLeft * unit.topSpeed)) {
      val shouldDrop = unit.matchups.framesOfSafety > unit.cooldownLeft || (unit.agent.shouldEngage && unit.matchups.threatsInRange.forall(u => u.pixelRangeAgainst(unit) + 16 >= unit.effectiveRangePixels))
      if (shouldDrop) {
        Attack.delegate(unit)
        return
      }
    }
    val target = unit.agent.toAttack

    def eligibleTeammate = (unit: UnitInfo) => ! unit.isAny(Protoss.Shuttle, Protoss.Reaver, Protoss.HighTemplar)
    lazy val squadmates = unit.squadmates.view.filter(eligibleTeammate)
    lazy val allies = unit.matchups.allies.filter(eligibleTeammate)
    lazy val alliesEngaged = allies.filter(_.matchups.enemies.nonEmpty)

    val destinationAir =
      unit.agent.toAttack.map(_.pixelCenter)
        .orElse(if (squadmates.size > 3) Some(PurpleMath.centroid(squadmates.map(_.pixelCenter))) else None)
        .orElse(if (allies.size > 3) Some(PurpleMath.centroid(allies.map(_.pixelCenter))) else None)
        .getOrElse(if (unit.agent.shouldEngage) unit.agent.destination else unit.agent.origin)
    val destination = destinationAir.nearestWalkableTerrain
    unit.agent.toTravel = Some(destination.pixelCenter)

    val targetDistance: Float = (unit.effectiveRangePixels + (if (unit.unitClass != Protoss.HighTemplar) unit.topSpeed * unit.cooldownLeft else 0)).toFloat / 32f
    val endDistanceMaximum = if (target.isDefined && unit.pixelDistanceCenter(target.get.pixelCenter) > targetDistance) targetDistance else 0
    val repulsors = Avoid.pathfindingRepulsion(unit)
    val shouldCrossTerrain = unit.matchups.framesOfSafety < 24 || unit.matchups.threats.isEmpty || target.forall(_.zone != unit.zone) || ! unit.tileIncludingCenter.walkable
    var path = NoPath.value
    Seq(Some(0), Some(With.grids.enemyRangeGround.addedRange - 1), None).foreach(maximumThreat =>
      (if (shouldCrossTerrain) Seq(true) else Seq(false, true)).foreach(crossEdges => {
        if ( ! path.pathExists) {
          val profile = new PathfindProfile(unit.tileIncludingCenter)
          profile.end                 = Some(destination)
          profile.endDistanceMaximum  = endDistanceMaximum // Uses the distance implied by allowGroundDist
          profile.lengthMaximum       = Some(30)
          profile.threatMaximum       = maximumThreat
          profile.canCrossUnwalkable  = crossEdges
          profile.allowGroundDist     = false
          profile.costOccupancy       = 0.5f
          profile.costThreat          = 5f
          profile.costRepulsion       = 2.5f
          profile.repulsors           = repulsors
          profile.unit                = Some(unit)
          path = profile.find
        }
      }))
    if (path.pathExists) {
      unit.agent.toTravel = Some(path.end.pixelCenter)
      new Traverse(path).delegate(unit)
    } else {
      Manners.debugChat(f"Failed to path $unit to $destination")
      Manners.debugChat(f"Targeting $target")
    }
  }
}
