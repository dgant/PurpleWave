package Information.Grids.ArrayTypes

import Lifecycle.With
import Mathematics.Points.Tile

abstract class AbstractGridVersioned extends AbstractGridArray[Int] {
  var version: Int = 0
  final override val defaultValue: Int = version - 1
  final override val values: Array[Int] = Array.fill(length)(defaultValue)
  reset()
  def updateVersion() { version = Math.max(1 + version, With.frame) }

  @inline final def isSet(i: Int): Boolean = get(i) >= version
  @inline final def isSet(tile: Tile): Boolean = isSet(tile.i)
  @inline final def isSetUnchecked(i: Int): Boolean = values(i) >= version
  @inline final def stamp(i: Int) { set(i, version) }
  @inline final def stampUnchecked(i: Int) { values(i) = version }
  @inline final def stamp(x: Int, y: Int) { set(x + y * With.mapTileWidth, version)}
  @inline final def stampUnchecked(x: Int, y: Int) { values(x + y * With.mapTileWidth) = version }
  @inline final def stamp(tile: Tile) { stamp(tile.i) }
  @inline final def stampUnchecked(tile: Tile) { stampUnchecked(tile.i) }
  @inline final def framesSince(tile: Tile): Int = version - get(tile)
  @inline final def ever(tile: Tile): Boolean = get(tile) > 0

  override def update() {
    updateVersion()
    updateCells()
  }

  override def repr(value: Int): String = if (value >= version) "true" else ""
  
  protected def updateCells() {}
}