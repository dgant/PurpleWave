package Micro.Actions.Protoss

import Information.Geography.Pathfinding.PathfindProfile
import Information.Geography.Pathfinding.Types.NoPath
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Tile
import Mathematics.Shapes.Spiral
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Targeting.Target
import Micro.Agency.Commander
import Micro.Coordination.Pathing.MicroPathing
import Planning.UnitMatchers.MatchWorker
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object BeReaver extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = Protoss.Reaver(unit)

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    considerHopping(unit)
    considerParadropping(unit)
  }

  def considerHopping(unit: FriendlyUnitInfo) {
    lazy val inRangeNeedlessly = unit.matchups.threatsInRange.exists(t => ! t.flying && t.pixelRangeAgainst(unit) < unit.pixelRangeAgainst(t))
    lazy val attackingSoon = unit.matchups.targetsInRange.nonEmpty && unit.cooldownLeft < Math.min(unit.cooldownMaxGround / 4, unit.matchups.framesToLive)
    if (unit.transport.isEmpty && inRangeNeedlessly && ! attackingSoon) {
      unit.agent.ride.foreach(Commander.rightClick(unit, _))
      if (Retreat.allowed(unit)) {
        Retreat.consider(unit)
        unit.agent.act("Hop")
      }
    }
  }

  def considerParadropping(unit: FriendlyUnitInfo) {
    if (unit.transport.isDefined)
    if (unit.agent.shouldEngage) Target.choose(unit)
    if (unit.agent.toAttack.isEmpty) return
    val target            = unit.agent.toAttack.get
    val destinationAir    = findFiringPosition(unit, target)
    val destinationGround = destinationAir.center.nearestWalkableTile

    // If we can drop out and attack now, do so
    val here = unit.tile
    var shouldDrop = false
    shouldDrop = shouldDrop || (unit.matchups.targetsInRange.exists(MatchWorker) && unit.matchups.threats.forall(t => MatchWorker(t) || (! t.canMove && t.pixelsToGetInRange(unit) > 32)))
    shouldDrop = shouldDrop || here.groundPixels(destinationGround) < 32
    shouldDrop = shouldDrop || unit.inRangeToAttack(target) && ! With.grids.enemyRangeGround.inRange(here) && ! unit.matchups.threats.exists(t => t.is(Terran.SiegeTankSieged) && t.inRangeToAttack(unit, here.center))
    if (shouldDrop) {
      Commander.attack(unit)
      return
    }

    unit.agent.toTravel = Some(destinationGround.center)

    val endDistanceMaximum = if (unit.is(Protoss.HighTemplar)) 0 else (unit.topSpeed * unit.cooldownLeft / 32).toInt
    val repulsors = MicroPathing.getPathfindingRepulsors(unit)
    var path = NoPath.value
    Seq(
      Some(With.grids.enemyRangeGround.defaultValue),
      Some(With.grids.enemyRangeGround.margin),
      None).foreach(maximumThreat =>
      if ( ! path.pathExists) {
        val profile = new PathfindProfile(unit.tile)
        profile.end                 = Some(destinationGround)
        profile.endDistanceMaximum  = endDistanceMaximum // Uses the distance implied by allowGroundDist
        profile.lengthMaximum       = Some(30)
        profile.threatMaximum       = maximumThreat
        profile.canCrossUnwalkable  = Some(true)
        profile.canEndUnwalkable    = Some(false)
        profile.costRepulsion       = 0.8f
        profile.repulsors           = repulsors
        profile.unit                = Some(unit)
        path = profile.find
      }
    )
    if (path.pathExists) {
      unit.agent.toTravel = Some(path.end.center)
      MicroPathing.tryMovingAlongTilePath(unit, path)
    } else {
      With.logger.warn(f"Failed to path $unit to $destinationGround")
      With.logger.warn(f"Targeting $target")
    }
  }

  def findFiringPosition(reaver: FriendlyUnitInfo, target: UnitInfo): Tile = {
    val firingDistance  = (reaver.effectiveRangePixels + Math.min(reaver.effectiveRangePixels, target.effectiveRangePixels)) / 2
    val originPixel     = reaver.pixel
    val goalTile        = target.projectFrames(reaver.cooldownLeft).tile
    val naiveTile       = goalTile.center.project(originPixel, firingDistance).nearestWalkableTile
    val naiveDistance   = target.pixel.tile.tileDistanceFast(naiveTile)
    val candidates      = Spiral.points(12)
      .view
      .map(naiveTile.add)
      .filter(t =>
        t.valid
        && t.walkable
        && With.grids.enemyRangeAirGround.get(t) == 0
        && t.groundPixels(goalTile) < 1.5 * reaver.effectiveRangePixels
        && ! reaver.matchups.threats.exists(t => t.is(Terran.SiegeTankSieged) && t.inRangeToAttack(reaver, t.pixel)))
      .map(tile => (
        tile,
        Math.abs(naiveDistance - tile.tileDistanceFast(goalTile)),
        tile.tileDistanceFast(originPixel.tile),
        With.grids.enemyRangeGround.get(tile),
        With.grids.enemyRangeAir.get(tile)))
      .map(p => ((p._2 + p._3) * (p._4 + p._5), p))
      .toVector
    val output = Maff.minBy(candidates)(_._1).map(_._2._1).getOrElse(naiveTile)
    output
  }
}
