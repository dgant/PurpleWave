package Information.Grids.ArrayTypes

import Lifecycle.With
import Mathematics.Points.Tile

abstract class AbstractGridVersioned extends AbstractGridInt {

  var version: Int = 0
  override val defaultValue: Int = -1
  reset()
  def updateVersion() { version += 1 }

  def isSet(i: Int): Boolean = get(i) >= version
  def isSet(tile: Tile): Boolean = isSet(tile.i)
  def stamp(i: Int) { set(i, version)}
  def stamp(x: Int, y: Int) { set(x + y * With.mapTileWidth, version)}
  def stamp(tile: Tile) { stamp(tile.i) }
  def framesSince(tile: Tile): Int = version - get(tile)

  override def update() {
    updateVersion()
    updateTimestamps()
  }

  override def repr(value: Int): String = if (value >= version) "true" else ""
  
  protected def updateTimestamps() {}
}