package Utilities.TileFilters
import Lifecycle.With
import Mathematics.Points.Tile

object TileAny extends TileFilter {
  override def apply(tile: Tile): Boolean = true
  override def isSupersetOf(other: TileFilter): Boolean = true
  override def generate: Iterable[Tile] = With.geography.allTiles
}
