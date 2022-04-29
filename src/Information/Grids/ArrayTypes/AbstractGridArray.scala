package Information.Grids.ArrayTypes

import Information.Grids.AbstractTypedGrid
import Lifecycle.With
import Mathematics.Points.Tile

abstract class AbstractGridArray[T] extends AbstractTypedGrid[T] {

  @inline def rawValues: Array[T] = values
  protected val values: Array[T]
  private var initialized = false
  
  def reset(): Unit = {
    val default = defaultValue
    var i = 0
    while (i < length) {
      values(i) = default
      i += 1
    }
  }

  final def isInitialized: Boolean = initialized
  final def initialize(): Unit = { if ( ! initialized) { onInitialization(); initialized = true } }
  override def update(): Unit = { initialize() }
  def onInitialization(): Unit = {}
  val indices: Range = 0 until length
  val tiles: Seq[Tile] = indices.map(i => new Tile(i))
  def set(i: Int, value: T)           : Unit  = if (valid(i)) values(i) = value
  def set(x: Int, y: Int, value: T)   : Unit  = set(x + y * With.mapTileWidth, value)
  def set(tile: Tile, value: T)       : Unit  = set(tile.i, value)
  def getUnchecked(i: Int)            : T     = values(i)
  def setUnchecked(i: Int, value: T)  : Unit  = values(i) = value
}

