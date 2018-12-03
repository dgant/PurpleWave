package Information.Grids.ArrayTypes

import Information.Grids.AbstractGrid
import Mathematics.Points.Tile

abstract class AbstractGridArray[T] extends AbstractGrid[T] {

  def rawValues: Array[T] = values
  protected var values: Array[T]
  private var initialized = false
  
  def reset() {
    val default = defaultValue
    
    //Use a while-loop because in Scala they are much faster than for-loops
    var i = 0
    while (i < length) {
      values(i) = default
      i += 1
    }
  }

  final def isInitialized: Boolean = initialized
  final def initialize()        { if ( ! initialized) { onInitialization(); initialized = true } }
  override def update()         { initialize() }
  def onInitialization()        {}
  val indices: Range            = 0 until length
  val tiles                     = indices.map(i => new Tile(i))
  def get(i: Int): T            = if (valid(i)) values(i) else defaultValue
  def set(i: Int, value: T)     { if (valid(i)) values(i) = value }
  def set(tile: Tile, value: T) { set(tile.i, value) }
}

