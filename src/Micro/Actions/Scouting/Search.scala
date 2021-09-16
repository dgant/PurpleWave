package Micro.Actions.Scouting

import Information.Geography.Pathfinding.PathfindProfile
import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Physics.Gravity
import Micro.Actions.Action
import Micro.Actions.Commands.Move
import Micro.Agency.Commander
import Micro.Coordination.Pathing.MicroPathing
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Search extends AbstractSearch {
  override protected val boredomFrames: Int = 24 * 30
}

object SearchWhenBored extends AbstractSearch {
  override protected val boredomFrames: Int = 24 * 3
}

abstract class AbstractSearch extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    With.geography.enemyBases.nonEmpty || unit.intent.toScoutTiles.nonEmpty
  }
  
  protected val boredomFrames: Int
  
  override protected def perform(unit: FriendlyUnitInfo) {
    if (unit.matchups.threats.isEmpty && ! unit.intent.toScoutTiles.exists(_.explored)) {
      Move.consider(unit)
    }

    def nearestNeutralBases(count: Int): Iterable[Base] = {
      With.geography.neutralBases
        .filterNot(_.zone.island)
        .sortBy(b =>
          b.townHallTile.pixelDistanceGround(With.scouting.mostBaselikeEnemyTile)
          - b.townHallTile.pixelDistanceGround(With.geography.home))
        .take(count)
    }

    val tilesToScout = unit.intent.toScoutTiles
      .filter(tile =>
        With.grids.lastSeen.framesSince(tile) > boredomFrames
        && With.grids.buildableTerrain.get(tile)
        && (unit.enemyRangeGrid.get(tile) <= 0 || ( unit.cloaked && ! tile.enemyDetected))
        && ! unit.matchups.threats.exists(_.inRangeToAttack(unit, tile.center))
        && tile.base.forall(base =>
          ! base.owner.isEnemy
          || ! base.owner.isZerg
          || tile.creep
          || tile.tileDistanceFast(base.townHallArea.midpoint) < 9.0))
  
    if (tilesToScout.isEmpty) return
  
    val pulls = tilesToScout.map(tile => Gravity(
      tile.center,
      With.grids.lastSeen.framesSince(tile)))
    
    val force = pulls.map(_.apply(unit.pixel)).reduce(_ + _)

    val target = unit.pixel.add(force.normalize(64.0).toPoint)
    val tileToScout = tilesToScout.minBy(_.center.pixelDistance(target))

    val profile = new PathfindProfile(unit.tile)
    profile.end               = Some(tileToScout)
    profile.employGroundDist  = true
    profile.costOccupancy     = 0.01
    profile.costRepulsion     = 5
    profile.repulsors         = MicroPathing.getPathfindingRepulsors(unit)
    profile.lengthMaximum     = Some(20)
    profile.unit              = Some(unit)
    val path = profile.find
    unit.agent.toTravel = Some(tileToScout.center)
    MicroPathing.tryMovingAlongTilePath(unit, path)
    Commander.move(unit)
  }
}
