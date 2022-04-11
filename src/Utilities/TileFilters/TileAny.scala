package Utilities.TileFilters
import Mathematics.Points.Tile

object TileAny extends TileFilter {
  override def apply(tile: Tile): Boolean = true
}
