package Information.Grids.Abstract.ArrayTypes

import Information.Grids.Abstract.Grid
import bwapi.TilePosition


abstract class GridArray[T] extends Grid[T] {
  
  protected var values:Array[T]
  private var initialized = false
  
  def defaultValue:T
  def repr(value:T):String
  
  def reset() = {
    val default = defaultValue
    val length = width * height
    
    //Use a while-loop because in Scala they are much faster than for-loops
    var i = 0
    while (i < length) {
      values(i) = default
      i += 1
    }
  }
  
  final def initialize()                                  = if ( ! initialized) { onInitialization(); initialized = true }
  override def update()                                   = initialize()
  def onInitialization() {}
  def indices:Iterable[Int]                               = values.indices
  def points:Iterable[(Int, Int)]                         = indices.map(i => (x(i), y(i)))
  def tiles:Iterable[TilePosition]                        = indices.map(i => new TilePosition(x(i), y(i)))
  def get(i:Int):T                                        = if (valid(i)) values(i) else defaultValue
  def set(i:Int, value:T):Unit                            = if (valid(i)) values(i) = value
  def set(tileX:Int, tileY:Int, value:T):Unit             = set(i(tileX, tileY), value)
  def set(position:TilePosition, value:T):Unit            = set(i(position.getX, position.getY), value)
}

