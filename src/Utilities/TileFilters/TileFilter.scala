package Utilities.TileFilters

import Mathematics.Points.Tile

trait TileFilter extends Function[Tile, Boolean] {
  def generate: Iterable[Tile] = Iterable.empty
}
