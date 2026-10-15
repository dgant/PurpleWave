package Information.Geography.Pathfinding

import Lifecycle.With
import Mathematics.Points.Tile

import scala.collection.mutable

trait GroundTopology {
  // Long-range distances describe the initial map, not newly constructed walls.
  // Retain that snapshot when neutral blockers disappear during the game.
  private lazy val walkable = Array.tabulate(With.mapTileArea)(i =>
    With.grids.walkable.get(i)
      || (With.grids.walkableTerrain.get(i) && With.units.ours.exists(_.tileArea.tiles.exists(_.i == i))))
  private val removedNeutralTiles = mutable.Set.empty[Int]
  private var topologyVersion = 0L
  final def groundTopologyVersion: Long = topologyVersion

  @inline final def groundWalkable(i: Int): Boolean = i >= 0 && i < walkable.length && walkable(i)

  final def onNeutralRemoved(tiles: Iterable[Tile]): Unit = {
    removedNeutralTiles ++= tiles.filter(_.valid).map(_.i)
  }

  final def updateGroundTopology(): Boolean = {
    if (removedNeutralTiles.isEmpty) return false
    // Stacked minerals must remain blocked until the last unit is removed.
    val occupied = With.units.neutral
      .filter(u => u.unitClass.isBuilding && ! u.flying)
      .flatMap(_.tiles.map(_.i)).toSet
    var changed = false
    removedNeutralTiles.foreach(i => {
      if ( ! walkable(i) && With.grids.walkableTerrain.get(i) && ! occupied.contains(i)) {
        walkable(i) = true
        changed = true
      }
    })
    removedNeutralTiles.clear()
    if (changed) topologyVersion += 1
    changed
  }
}
