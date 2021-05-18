package Information.Grids

import Lifecycle.With
import Mathematics.Points.Tile

abstract class AbstractGrid[T] {
  final val width: Int = With.mapTileWidth
  final val height: Int = With.mapTileHeight
  final val length: Int = width * height
  @inline final def valid(i: Int): Boolean = i >= 0 && i < length
  @inline final def i(tileX: Int, tileY: Int): Int = tileX + tileY * width
  @inline final def get(tile: Tile): T = get(tile.i)
  @inline final def get(i: Int): T = if (valid(i)) getUnchecked(i) else defaultValue
  @inline final def apply(i: Int): T = get(i)
  @inline final def apply(tile: Tile): T = get(tile)
  @inline final def getUnchecked(tile: Tile): T = getUnchecked(tile.i)
  def getUnchecked(i: Int): T
  val defaultValue: T
  def repr(value: T): String = value.toString

  def update() {}
}
