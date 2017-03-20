package Information.Grids.Abstract

import bwapi.TilePosition


abstract class GridArray[T] extends Grid[T] {
  
  protected val values:Array[T]
  private var initialized = false
  
  override def update(tiles:Iterable[TilePosition]) = initialize()
  
  def defaultValue:T
  def repr(value:T):String
  def reset(relevantTiles:Iterable[TilePosition]) = relevantTiles.foreach(tile => set(tile, defaultValue))
  final def initialize()                          = if ( ! initialized) { onInitialization(); initialized = true }
  def onInitialization()                          {}
  def indices:Iterable[Int]                       = values.indices
  def points:Iterable[(Int, Int)]                 = indices.map(i => (x(i), y(i)))
  def tiles:Iterable[TilePosition]                = indices.map(i => new TilePosition(x(i), y(i)))
  def get(i:Int):T                                = if (valid(i)) values(i) else defaultValue
  def set(i:Int, value:T):Unit                    = if (valid(i)) values(i) = value
  def set(tileX:Int, tileY:Int, value:T):Unit     = set(i(tileX, tileY), value)
  def set(position:TilePosition, value:T):Unit    = set(i(position.getX, position.getY), value)
}

