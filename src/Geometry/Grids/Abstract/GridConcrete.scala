package Geometry.Grids.Abstract

import bwapi.TilePosition


abstract class GridConcrete[T] extends Grid[T] {
  val _positions:Array[T]
  var _initialized = false
  
  override def update() = initialize()
  
  def defaultValue:T
  def repr(value:T):String
  def reset()                                  = indices.foreach(_positions(_) = defaultValue)
  final def initialize()                       = if ( ! _initialized) { onInitialization(); _initialized = true }
  def onInitialization()                       {}
  def indices:Iterable[Int]                    = _positions.indices
  def points:Iterable[(Int, Int)]              = indices.map(i => (x(i), y(i)))
  def positions:Iterable[TilePosition]         = indices.map(i => new TilePosition(x(i), y(i)))
  def get(i:Int):T                             = if (valid(i)) _positions(i) else defaultValue
  def set(i:Int, value:T):Unit                 = if (valid(i)) _positions(i) = value
  def set(tileX:Int, tileY:Int, value:T):Unit  = set(i(tileX, tileY), value)
  def set(position:TilePosition, value:T):Unit = set(i(position.getX, position.getY), value)
}

