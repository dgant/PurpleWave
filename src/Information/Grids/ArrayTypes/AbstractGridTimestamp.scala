package Information.Grids.ArrayTypes

import Lifecycle.With
import Mathematics.Points.Tile

abstract class AbstractGridTimestamp extends AbstractGridInt {

  def isSet(i: Int): Boolean = get(i) >= frameUpdated
  def isSet(tile: Tile): Boolean = isSet(tile.i)
  
  var frameUpdated = 0
  val never: Int = -24 * 60 * 60
  
  override val defaultValue: Int = never
  reset()

  def stamp(i: Int) { set(i, With.frame)}
  def stamp(x: Int, y: Int) { set(x + y * With.mapTileWidth, With.frame)}
  def stamp(tile: Tile) { stamp(tile.i) }
  def framesSince(tile: Tile): Int = frameUpdated - get(tile)
  
  protected def needsUpdate: Boolean = true
  
  final override def update() {
    if (needsUpdate) {
      frameUpdated = With.frame
      updateTimestamps()
    }
  }
  
  override def repr(value: Int): String = if (value >= frameUpdated) "true" else ""
  
  protected def updateTimestamps()
}