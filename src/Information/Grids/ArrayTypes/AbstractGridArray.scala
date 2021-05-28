package Information.Grids.ArrayTypes

import Information.Grids.AbstractTypedGrid
import Mathematics.Points.Tile

abstract class AbstractGridArray[T] extends AbstractTypedGrid[T] {

  @inline def rawValues: Array[T] = values
  protected val values: Array[T]
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
  final def initialize() { if ( ! initialized) { onInitialization(); initialized = true } }
  override def update() { initialize() }
  def onInitialization() {}
  val indices: Range  = 0 until length
  val tiles: Seq[Tile] = indices.map(i => new Tile(i))
  def set(i: Int, value: T)           : Unit  = if (valid(i)) values(i) = value
  def set(tile: Tile, value: T)       : Unit  = set(tile.i, value)
  def getUnchecked(i: Int)            : T     = values(i)
  def setUnchecked(i: Int, value: T)  : Unit  = values(i) = value
}

