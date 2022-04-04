package Utilities.TileFilters

import Mathematics.Points.Tile

trait TileFilter extends Function[Tile, Boolean] {
  def isSupersetOf(other: TileFilter): Boolean = this == other
  def isSubsetOf(other: TileFilter): Boolean = false
  def generate: Iterable[Tile] = Iterable.empty
}
