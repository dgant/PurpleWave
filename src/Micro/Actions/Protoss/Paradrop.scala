package Micro.Actions.Protoss

import Information.Geography.Pathfinding.PathfindProfile
import Information.Geography.Pathfinding.Types.NoPath
import Lifecycle.{Manners, With}
import Mathematics.Points.Tile
import Mathematics.PurpleMath
import Mathematics.Shapes.Spiral
import Micro.Actions.Action
import Micro.Actions.Combat.Targeting.Target
import Micro.Coordination.Pathing.MicroPathing
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

object Paradrop extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.transport.isDefined && unit.is(Protoss.Reaver)

  def findFiringPosition(reaver: FriendlyUnitInfo, target: UnitInfo): Tile = {
    val firingDistance  = (reaver.effectiveRangePixels + Math.min(reaver.effectiveRangePixels, target.effectiveRangePixels)) / 2
    val originPixel     = reaver.pixelCenter
    val goalTile        = target.projectFrames(reaver.cooldownLeft).tileIncluding
    val naiveTile       = goalTile.pixelCenter.project(originPixel, firingDistance).nearestWalkableTile
    val naiveDistance   = target.pixelCenter.tileIncluding.tileDistanceFast(naiveTile)
    val candidates = Spiral.points(7)
      .view
      .map(naiveTile.add)
      .filter(t =>
        t.valid
        && With.grids.walkable.get(t)
        && With.grids.enemyRangeAirGround.get(t) == 0
        && t.groundPixels(goalTile) < 1.5 * reaver.effectiveRangePixels
        && ! reaver.matchups.threats.exists(t => t.isSiegeTankSieged() && t.inRangeToAttack(reaver, t.pixelCenter)))
      .map(tile => (
        tile,
        Math.abs(naiveDistance - tile.tileDistanceFast(goalTile)),
        tile.tileDistanceFast(originPixel.tileIncluding),
        With.grids.enemyRangeGround.get(tile),
        With.grids.enemyRangeAir.get(tile)))
      .map(p => ((p._2 + p._3) * (p._4 + p._5), p))
      .toVector
    val output = ByOption.minBy(candidates)(_._1).map(_._2._1).getOrElse(naiveTile)
    output
  }

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    if (unit.agent.shouldEngage) {
      Target.choose(unit)
    }
    val target = unit.agent.toAttack
    def eligibleTeammate  = (unit: UnitInfo) => ! unit.isAny(Protoss.Shuttle, Protoss.Reaver, Protoss.HighTemplar)
    def isEngaged         = (unit: UnitInfo) => unit.matchups.enemies.nonEmpty
    def canFollow         = (units: Iterable[UnitInfo]) => units.size > 2
    val squadmates        = unit.squadmates.view.filter(eligibleTeammate)
    val squadmatesEngaged = squadmates.filter(isEngaged)
    val allies            = unit.matchups.allies.filter(eligibleTeammate)
    val alliesEngaged     = allies.filter(isEngaged)
    val toFollow          = Seq(squadmatesEngaged, alliesEngaged, squadmates, allies).find(canFollow)
    val destinationAir    = unit.agent.toAttack.map(findFiringPosition(unit, _))
      .orElse(toFollow.map(folks => PurpleMath.centroid(folks.map(_.pixelCenter)).tileIncluding))
      .getOrElse((if (unit.agent.shouldEngage) unit.agent.destination else unit.agent.origin).tileIncluding)
    val destinationGround = destinationAir.pixelCenter.nearestWalkableTile

    // If we can drop out and attack now, do so
    if (target.isDefined) {
      var shouldDrop = false
      val here = unit.tileIncludingCenter
      shouldDrop = shouldDrop || here.tileDistanceSquared(destinationGround) < 4 && here.zone == destinationGround.zone
      shouldDrop = shouldDrop || unit.inRangeToAttack(target.get) && With.grids.enemyRangeGround.get(here) == 0 && ! unit.matchups.threats.exists(t => t.isSiegeTankSieged() && t.inRangeToAttack(unit, here.pixelCenter))
      if (shouldDrop) {
        With.commander.attack(unit)
        return
      }
    }

    unit.agent.toTravel = Some(destinationGround.pixelCenter)

    val endDistanceMaximum = if (unit.is(Protoss.HighTemplar)) 0 else (unit.topSpeed * unit.cooldownLeft / 32).toInt
    val repulsors = MicroPathing.getPathfindingRepulsors(unit)
    var path = NoPath.value
    Seq(
      Some(With.grids.enemyRangeGround.defaultValue),
      Some(With.grids.enemyRangeGround.addedRange - 1),
      None).foreach(maximumThreat =>
      if ( ! path.pathExists) {
        val profile = new PathfindProfile(unit.tileIncludingCenter)
        profile.end                 = Some(destinationGround)
        profile.endDistanceMaximum  = endDistanceMaximum // Uses the distance implied by allowGroundDist
        profile.lengthMaximum       = Some(30)
        profile.threatMaximum       = maximumThreat
        profile.canCrossUnwalkable  = Some(true)
        profile.canEndUnwalkable    = Some(false)
        profile.costThreat          = if (target.isDefined) 0.5f else 3f
        profile.costRepulsion       = if (target.isDefined) 0.5f else 6f
        profile.repulsors           = repulsors
        profile.unit                = Some(unit)
        path = profile.find
      }
    )
    if (path.pathExists) {
      if (unit.pixelDistanceSquared(path.end.pixelCenter) <= 16 * 16) {
        unit.transport.foreach(With.commander.unload(_, unit))
        With.commander.doNothing(unit)
      } else {
        unit.agent.toTravel = Some(path.end.pixelCenter)
        MicroPathing.tryMovingAlongTilePath(unit, path)
      }
    } else {
      Manners.debugChat(f"Failed to path $unit to $destinationGround")
      Manners.debugChat(f"Targeting $target")
    }
  }
}
