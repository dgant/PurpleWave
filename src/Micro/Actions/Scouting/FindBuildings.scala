package Micro.Actions.Scouting

import Information.Geography.Pathfinding.PathfindProfile
import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Physics.Gravity
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Traverse
import Micro.Actions.Combat.Techniques.Avoid
import Micro.Actions.Commands.Move
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import bwapi.Race

object FindBuildings extends AbstractFindBuildings {
  override protected val boredomFrames = 24 * 30
}

object FindBuildingsWhenBored extends AbstractFindBuildings {
  override protected val boredomFrames = 24 * 3
}

abstract class AbstractFindBuildings extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    With.geography.enemyBases.nonEmpty || unit.agent.lastIntent.toScoutBases.nonEmpty
  }
  
  protected val boredomFrames: Int
  
  override protected def perform(unit: FriendlyUnitInfo) {
    def nearestNeutralBases(count: Int): Iterable[Base] = {
      With.geography.neutralBases
        .filterNot(_.zone.island)
        .sortBy(b =>
          b.townHallTile.groundPixels(With.intelligence.mostBaselikeEnemyTile)
          - b.townHallTile.groundPixels(With.geography.home))
        .take(count)
    }

    val suggestedBases = unit.agent.lastIntent.toScoutBases
    val basesToScout = if (suggestedBases.nonEmpty) suggestedBases else
      With.geography.enemyBases.filter(b => ! b.zone.island && With.intelligence.unitsShown(b.owner, Zerg.SpawningPool) == 0) ++ (
        if (With.geography.enemyBases.size == 1)
          With.geography.enemyBases.head.natural.map(Vector(_)).getOrElse(nearestNeutralBases(1))
        else if (With.geography.enemyBases.size <= With.geography.ourBases.size)
          nearestNeutralBases(if (With.enemy.isZerg) 5 else 2)
        else
          Vector.empty)
  
    val tilesToScout = basesToScout
      .flatMap(base => {
        val tiles = base.zone.tiles.filter(tile =>
          ! base.harvestingArea.contains(tile) //Don't walk into worker line
          && With.grids.walkable.get(tile)
          && With.grids.friendlyVision.framesSince(tile) > boredomFrames)
      
        if (base.owner.raceInitial == Race.Zerg) {
          tiles.filter(tile => With.grids.creep.get(tile) || tile.tileDistanceFast(base.townHallArea.midpoint) < 9.0)
        }
        else {
          tiles
        }
      })
      .filter(With.grids.buildableTerrain.get)
      .filter(tile => unit.enemyRangeGrid.getUnchecked(tile.i) <= 0 || ( unit.cloaked && ! With.grids.enemyDetection.isDetected(tile.i)))
      .filter(tile => ! unit.matchups.threats.exists(_.inRangeToAttack(unit, tile.pixelCenter)))
  
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
    profile.allowGroundDist     = false
    profile.costOccupancy       = 0.01f
    profile.costThreat          = 5
    profile.costRepulsion       = 0.25f
    profile.repulsors           = Avoid.pathfindingRepulsion(unit)
    profile.unit                = Some(unit)
    val path = profile.find
    unit.agent.toTravel = Some(tileToScout.pixelCenter)
    new Traverse(path).delegate(unit)
    Move.delegate(unit)
  }
}
