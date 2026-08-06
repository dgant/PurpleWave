package Placement.Generation

import Mathematics.Points.Tile

class TileGeneratorSpecific(tiles: Iterable[Tile]) extends TileGenerator {
  private val iterator = tiles.iterator

  override def next(): Tile = iterator.next()
  override def hasNext: Boolean = iterator.hasNext
}
