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
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    if (unit.matchups.threats.isEmpty && ! unit.intent.toScoutTiles.exists(_.explored)) {
      Move.apply(unit)
      return
    }

    def nearestNeutralBases(count: Int): Iterable[Base] = {
      With.geography.neutralBases
        .filterNot(_.zone.island)
        .sortBy(b =>
          b.townHallTile.groundPixels(With.scouting.enemyHome)
          - b.townHallTile.groundPixels(With.geography.home))
        .take(count)
    }

    val bannedTiles = unit.intent.toScoutTiles.view
      .flatMap(_.base).toSet.filter(b => b.isEnemy && b.scoutedByUs)
      .flatMap(_.harvestingTrafficTiles)

    val tilesToScout = unit.intent.toScoutTiles
      .filter(tile =>
        With.grids.lastSeen.framesSince(tile) > boredomFrames
        && tile.buildableTerrain
        && (tile.enemyRangeAgainst(unit) <= 0 || ( unit.cloaked && ! tile.enemyDetected))
        && ! bannedTiles.contains(tile)
        && tile.base.forall(base =>
          ! base.owner.isEnemy
          || ! base.owner.isZerg
          || tile.creep
          || tile.tileDistanceFast(base.townHallArea.midpoint) < 9.0))
  
    if (tilesToScout.isEmpty) return
    val force = tilesToScout.view.map(tile => Gravity(tile.center, With.grids.lastSeen.framesSince(tile))).map(_.apply(unit.pixel)).reduce(_ + _)

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
    profile.alsoUnwalkable    = bannedTiles
    val path = profile.find
    unit.agent.toTravel = Some(tileToScout.center)
    MicroPathing.tryMovingAlongTilePath(unit, path)
    Commander.move(unit)
  }
}
