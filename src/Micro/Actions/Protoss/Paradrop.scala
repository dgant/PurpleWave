package Micro.Actions.Protoss

import Information.Geography.Pathfinding.PathfindProfile
import Information.Geography.Pathfinding.Types.NoPath
import Lifecycle.{Manners, With}
import Mathematics.Points.Tile
import Mathematics.PurpleMath
import Mathematics.Shapes.Spiral
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Traverse
import Micro.Actions.Combat.Targeting.Target
import Micro.Actions.Commands.Attack
import Micro.Coordination.Pathing.MicroPathing
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Paradrop extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.transport.isDefined && unit.isAny(Protoss.Reaver, Protoss.HighTemplar)

  def findFiringPosition(reaver: FriendlyUnitInfo, target: UnitInfo): Tile = {
    val firingDistance  = (reaver.effectiveRangePixels + Math.min(reaver.effectiveRangePixels, target.effectiveRangePixels)) / 2
    val originTile      = reaver.tileIncludingCenter
    val goalTile        = target.projectFrames(reaver.cooldownLeft).tileIncluding
    val naiveTile       = goalTile.pixelCenter.project(originTile.pixelCenter, firingDistance).nearestWalkableTerrain
    val naiveDistance   = target.pixelCenter.tileIncluding.tileDistanceFast(naiveTile)
    val candidates = Spiral.points(7)
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
        tile.tileDistanceFast(originTile),
        With.grids.enemyRangeGround.get(tile),
        With.grids.enemyRangeAir.get(tile)))
      .map(p => ((p._2 + p._3) * (p._4 + p._5), p))
    if (candidates.isEmpty) {
      return naiveTile
    }
    val output = candidates.minBy(_._1)._2._1
    output
  }

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

    val target = unit.agent.toAttack
    def eligibleTeammate = (unit: UnitInfo) => ! unit.isAny(Protoss.Shuttle, Protoss.Reaver, Protoss.HighTemplar)
    def isEngaged = (unit: UnitInfo) => unit.matchups.enemies.nonEmpty
    def canFollow = (units: Iterable[UnitInfo]) => units.size > 2
    val squadmates        = unit.squadmates.view.filter(eligibleTeammate)
    val squadmatesEngaged = squadmates.filter(isEngaged)
    val allies            = unit.matchups.allies.filter(eligibleTeammate)
    val alliesEngaged     = allies.filter(isEngaged)
    val toFollow          = Seq(squadmatesEngaged, alliesEngaged, squadmates, allies).find(canFollow)
    val destinationAir    = unit.agent.toAttack.map(findFiringPosition(unit, _))
      .orElse(toFollow.map(folks => PurpleMath.centroid(folks.map(_.pixelCenter)).tileIncluding))
      .getOrElse((if (unit.agent.shouldEngage) unit.agent.destination else unit.agent.origin).tileIncluding)
    val destinationGround = destinationAir.pixelCenter.nearestWalkableTerrain

    // If we can drop out and attack now, do so
    if (target.isDefined) {
      var shouldDrop = false
      val here = unit.tileIncludingCenter
      shouldDrop = shouldDrop || here.tileDistanceSquared(destinationGround) < 4 && here.zone == destinationGround.zone
      shouldDrop = shouldDrop || unit.inRangeToAttack(target.get) && With.grids.enemyRangeGround.get(here) == 0 && ! unit.matchups.threats.exists(t => t.isSiegeTankSieged() && t.inRangeToAttack(unit, here.pixelCenter))
      if (shouldDrop) {
        Attack.delegate(unit)
        return
      }
    }

    unit.agent.toTravel   = Some(destinationGround.pixelCenter)

    val targetDistance: Float = (unit.effectiveRangePixels + (if (unit.unitClass != Protoss.HighTemplar) unit.topSpeed * unit.cooldownLeft else 0)).toFloat / 32f
    val endDistanceMaximum = if (target.isDefined && unit.pixelDistanceCenter(target.get.pixelCenter) > targetDistance) targetDistance else 0
    val repulsors = MicroPathing.getPathfindingRepulsors(unit)
    var path = NoPath.value
    var crossTerrainOptions = if (unit.matchups.threatsInRange.nonEmpty) Seq(true) else Seq(false, true)
    Seq(Some(0), Some(With.grids.enemyRangeGround.addedRange - 1), None).foreach(maximumThreat =>

      if ( ! path.pathExists) {
        val profile = new PathfindProfile(unit.pixelCenter.nearestWalkableTerrain)
        profile.end                 = Some(destinationGround)
        profile.endDistanceMaximum  = endDistanceMaximum // Uses the distance implied by allowGroundDist
        profile.lengthMaximum       = Some(30)
        profile.threatMaximum       = maximumThreat
        profile.canCrossUnwalkable  = true
        profile.costOccupancy       = 0.25f
        profile.costThreat          = 5f
        profile.costRepulsion       = if (target.isDefined) 0.5f else 2f
        profile.repulsors           = repulsors
        profile.unit                = Some(unit)
        path = profile.find
      }
    )
    if (path.pathExists) {
      unit.agent.toTravel = Some(path.end.pixelCenter)
      new Traverse(path).delegate(unit)
    } else {
      Manners.debugChat(f"Failed to path $unit to $destinationGround")
      Manners.debugChat(f"Targeting $target")
    }
  }
}
