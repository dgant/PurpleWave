package Geometry.Grids.Abstract

import Startup.With
import bwapi.{Position, TilePosition, WalkPosition}


abstract class Grid[T] {
  
  //Could reasonably switch to 8 to do WalkPosition
  val _pointsPerTile = 1
  val _width = _pointsPerTile * With.game.mapWidth
  val _height = _pointsPerTile * With.game.mapHeight
  val _positions:Array[T]
  
  def _defaultValue:T
  def reset() {
    indices.foreach(_positions(_) = _defaultValue)
  }
  
  var _initialized = false
  def initialize() { if ( ! _initialized) { onInitialization(); _initialized = true }}
  def onInitialization() {}
  def update() { initialize() }
  def indices:Iterable[Int] = {
    _positions.indices
  }

  def points:Iterable[(Int, Int)] = {
    indices.map(i => (x(i), y(i)))
  }
  def positions:Iterable[TilePosition] = {
    indices.map(i => new TilePosition(x(i), y(i)))
  }
  def x(index:Int):Int = {
    index % _width
  }
  def y(index:Int):Int = {
    index / _width
  }
  def _get(i:Int):T = {
    if (i < 0 || i >= _positions.length) return _defaultValue
    _positions(i)
  }
  def _get(x:Int, y:Int):T = {
    _get(x + y * _width)
  }
  def get(position:Position):T = {
    _get(_pointsPerTile*position.getX/32, _pointsPerTile*position.getY/32)
  }
  def get(position:WalkPosition):T = {
    _get(_pointsPerTile*position.getX/8, _pointsPerTile*position.getY/8)
  }
  def get(position:TilePosition):T = {
    _get(_pointsPerTile*position.getX, _pointsPerTile*position.getY)
  }
  def set(i:Int, value:T) {
    if (i < 0 || i >= _positions.length) return
    _positions(i) = value
  }
  def _set(x:Int, y:Int, value:T) {
    set(x + y * _width, value)
  }
  def setPosition(x:Int, y:Int, value:T) {
    _set(_pointsPerTile*x/32, _pointsPerTile*y/32, value)
  }
  def setWalkPosition(walkX:Int, walkY:Int, value:T) {
    _set(_pointsPerTile*walkX/4, _pointsPerTile*walkY/4, value)
  }
  def setTilePosition(tileX:Int, tileY:Int, value:T) {
    _set(tileX, tileY, value)
  }
  def set(position:Position, value:T) {
    setPosition(position.getX, position.getY, value)
  }
  def set(position:WalkPosition, value:T) {
    setWalkPosition(position.getX, position.getY, value)
  }
  def set(position:TilePosition, value:T) {
    setTilePosition(position.getX, position.getY, value)
  }
  
  def repr(value:T):String
}

