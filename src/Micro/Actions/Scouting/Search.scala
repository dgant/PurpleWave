package Micro.Actions.Scouting

import Debugging.Visualizations.Forces
import Debugging.Visualizations.Views.Micro.ShowPathfinding
import Information.Geography.Pathfinding.PathfindProfile
import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Physics.Gravity
import Mathematics.Points.Tile
import Micro.Actions.Action
import Micro.Agency.Commander
import Micro.Coordination.Pathing.MicroPathing
import Micro.Heuristics.Potential
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.?

object Search extends AbstractSearch {
  override protected val boredomFrames: Int = 24 * 30
}

object SearchWhenBored extends AbstractSearch {
  override protected val boredomFrames: Int = 24 * 3
}

abstract class AbstractSearch extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean =  With.geography.enemyBases.nonEmpty || unit.intent.toScoutTiles.nonEmpty
  
  protected val boredomFrames: Int
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    if (unit.matchups.threats.isEmpty && ! unit.intent.toScoutTiles.exists(_.explored)) {
      Commander.move(unit)
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

    val bannedTiles: Set[Tile] = ?(unit.flying,
      unit.intent.toScoutTiles
        .filter(_.enemyRangeAir > 0)
        .toSet,
      unit.intent.toScoutTiles
        .view
        .flatMap(_.base)
        .toSet
        .filter(b => b.isEnemy && b.scoutedByUs)
        .flatMap(_.harvestingTrafficTiles))

    val tilesToScout = unit.intent.toScoutTiles
      .filter(tile =>
        With.grids.lastSeen.framesSince(tile) > boredomFrames
        && tile.buildableTerrain
        && tile.walkable
        && (tile.enemyRangeAgainst(unit) <= 0 || ( unit.cloaked && ! tile.enemyDetected))
        && ! bannedTiles.contains(tile)
        && tile.base.forall(base =>
          ! base.owner.isEnemy
          || ! base.owner.isZerg
          || tile.creep
          || tile.tileDistanceFast(base.townHallArea.midpoint) < 9.0))
  
    if (tilesToScout.isEmpty) return
    val force = tilesToScout.view.map(tile => Gravity(tile.center, With.grids.lastSeen.framesSince(tile))).map(_(unit.pixel)).reduce(_ + _)

    val target = unit.pixel.add(force.normalize(64.0).toPoint)
    unit.agent.decision.set(tilesToScout.minBy(_.center.pixelDistance(target)).center)

    val profile               = new PathfindProfile(unit.tile)
    profile.end               = Some(unit.agent.destinationNext().tile)
    profile.employGroundDist  = ! unit.flying
    profile.costRepulsion     = 5
    profile.repulsors         = MicroPathing.getPathfindingRepulsors(unit)
    profile.lengthMaximum     = Some(20)
    profile.unit              = Some(unit)
    profile.alsoUnwalkable    = bannedTiles
    profile.debug             = ShowPathfinding.inUse && unit.selected
    val path                  = profile.find

    if (unit.zone == unit.agent.destinationNext().zone && ! unit.zone.edges.exists(_.contains(unit.pixel))) {
      unit.agent.forces(Forces.travel)  = Potential.towards(unit, MicroPathing.getWaypointAlongTilePath(unit, path).getOrElse(unit.agent.destinationNext()))
      unit.agent.forces(Forces.threat)  = Potential.hardAvoidThreatRange(unit)
      unit.agent.forces(Forces.pushing) = unit.velocity.normalize(0.5)
      unit.agent.destinationNext.setWaypoint(MicroPathing.getWaypointInDirection(unit, unit.agent.forces.sum.radians))
    } else {
      MicroPathing.tryMovingAlongTilePath(unit, path)
    }
    Commander.move(unit)
  }
}
