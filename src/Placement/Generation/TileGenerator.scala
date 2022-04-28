package Placement.Generation

import Mathematics.Points.Tile

trait TileGenerator {
  def next(): Tile
  def hasNext: Boolean
}
