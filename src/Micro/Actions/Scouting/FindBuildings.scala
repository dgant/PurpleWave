package Micro.Actions.Scouting

import Information.Geography.Pathfinding.PathfindProfile
import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Physics.Gravity
import Micro.Actions.Action
import Micro.Actions.Commands.Move
import Micro.Coordination.Pathing.MicroPathing
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object FindBuildings extends AbstractFindBuildings {
  override protected val boredomFrames = 24 * 30
}

object FindBuildingsWhenBored extends AbstractFindBuildings {
  override protected val boredomFrames = 24 * 3
}

abstract class AbstractFindBuildings extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    With.geography.enemyBases.nonEmpty || unit.agent.lastIntent.toScoutTiles.nonEmpty
  }
  
  protected val boredomFrames: Int
  
  override protected def perform(unit: FriendlyUnitInfo) {
    if (unit.matchups.threats.isEmpty && ! unit.agent.lastIntent.toScoutTiles.exists(With.grids.friendlyVision.ever)) {
      Move.consider(unit)
    }

    def nearestNeutralBases(count: Int): Iterable[Base] = {
      With.geography.neutralBases
        .filterNot(_.zone.island)
        .sortBy(b =>
          b.townHallTile.groundPixels(With.scouting.mostBaselikeEnemyTile)
          - b.townHallTile.groundPixels(With.geography.home))
        .take(count)
    }

    val tilesToScout = unit.agent.lastIntent.toScoutTiles
      .filter(tile =>
        With.grids.friendlyVision.framesSince(tile) > boredomFrames
        && With.grids.buildableTerrain.get(tile)
        && (unit.enemyRangeGrid.get(tile) <= 0 || ( unit.cloaked && ! With.grids.enemyDetection.isDetected(tile.i)))
        && ! unit.matchups.threats.exists(_.inRangeToAttack(unit, tile.pixelCenter))
        && tile.base.forall(base =>
          ! base.owner.isEnemy
          || ! base.owner.isZerg
          || With.grids.creep.get(tile)
          || tile.tileDistanceFast(base.townHallArea.midpoint) < 9.0))
  
    if (tilesToScout.isEmpty) return
  
    val pulls = tilesToScout.map(tile => Gravity(
      tile.pixelCenter,
      With.grids.friendlyVision.framesSince(tile)))
    
    val force = pulls.map(_.apply(unit.pixelCenter)).reduce(_ + _)

    val target = unit.pixelCenter.add(force.normalize(64.0).toPoint)
    val tileToScout = tilesToScout.minBy(_.pixelCenter.pixelDistance(target))

    val profile = new PathfindProfile(unit.tileIncludingCenter)
    profile.end                 = Some(tileToScout)
    profile.canCrossUnwalkable  = unit.flying
    profile.allowGroundDist     = true
    profile.costOccupancy       = 0.01f
    profile.costThreat          = 5
    profile.costRepulsion       = 0.4f
    profile.repulsors           = MicroPathing.getPathfindingRepulsors(unit)
    profile.lengthMaximum       = Some(20)
    profile.unit                = Some(unit)
    val path = profile.find
    unit.agent.toTravel = Some(tileToScout.pixelCenter)
    MicroPathing.tryMovingAlongTilePath(unit, path)
    Move.delegate(unit)
  }
}
