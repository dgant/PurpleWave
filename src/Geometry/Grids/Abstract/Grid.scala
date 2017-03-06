package Geometry.Grids.Abstract

import Startup.With
import bwapi.TilePosition


abstract class Grid[T] {
  val _width = With.game.mapWidth
  val _height = With.game.mapHeight
  val _positions:Array[T]
  var _initialized = false
  
  def _defaultValue:T
  def repr(value:T):String
  def reset()                                         = indices.foreach(_positions(_) = _defaultValue)
  def initialize()                             = if ( ! _initialized) { onInitialization(); _initialized = true }
  def onInitialization()                       {}
  def update()                                 = initialize()
  def indices:Iterable[Int]                    = _positions.indices
  def points:Iterable[(Int, Int)]              = indices.map(i => (x(i), y(i)))
  def positions:Iterable[TilePosition]         = indices.map(i => new TilePosition(x(i), y(i)))
  def valid(i:Int):Boolean                     = i > 0 && i < _positions.length
  def i(tileX:Int, tileY:Int)                  = tileX + tileY * _width
  def x(i:Int):Int                             = i % _width
  def y(i:Int):Int                             = i / _width
  def get(i:Int):T                             = if (valid(i)) _positions(i) else _defaultValue
  def get(tileX:Int, tileY:Int):T              = get(i(tileX, tileY))
  def get(tile: TilePosition):T                = get(i(tile.getX, tile.getY))
  def set(i:Int, value:T):Unit                 = if (valid(i)) _positions(i) = value
  def set(tileX:Int, tileY:Int, value:T):Unit  = set(i(tileX, tileY), value)
  def set(position:TilePosition, value:T):Unit = set(i(position.getX, position.getY), value)
}

