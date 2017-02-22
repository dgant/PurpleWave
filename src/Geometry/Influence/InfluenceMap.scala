package Geometry.Influence

import Startup.With
import bwapi.{Position, TilePosition, WalkPosition}

abstract class InfluenceMap {
  
  //Could reasonably switch to 8 to do WalkPosition
  val _pointsPerTile = 1
  val _width = _pointsPerTile * With.game.mapWidth
  val _height = _pointsPerTile * With.game.mapHeight
  val _positions = Array.fill[Int](_width * _height)(0)
  
  def update();
  def indices:Iterable[Int] = { _positions.indices }
  
  def reset() {
    indices.foreach(_positions(_) = 0)
  }
  def x(index:Int):Int = {
    index % _width
  }
  def y(index:Int):Int = {
    index / _width
  }
  def _get(i:Int):Int = {
    if (i < 0 || i >= _positions.length) return 0
    _positions(i)
  }
  def _get(x:Int, y:Int):Int = {
    _get(x + y * _width)
  }
  def get(position:Position):Int = {
    _get(_pointsPerTile*position.getX/32, _pointsPerTile*position.getY/32)
  }
  def get(position:WalkPosition):Int = {
    _get(_pointsPerTile*position.getX/8, _pointsPerTile*position.getY/8)
  }
  def get(position:TilePosition):Int = {
    _get(_pointsPerTile*position.getX, _pointsPerTile*position.getY)
  }
  def set(i:Int, value:Int) {
    if (i < 0 || i >= _positions.length) return
    _positions(i) = value
  }
  def _set(x:Int, y:Int, value:Int) {
    set(x + y * _width, value)
  }
  def setPosition(x:Int, y:Int, value:Int) {
    _set(_pointsPerTile*x/32, _pointsPerTile*y/32, value)
  }
  def setWalkPosition(walkX:Int, walkY:Int, value:Int) {
    _set(_pointsPerTile*walkX/4, _pointsPerTile*walkY/4, value)
  }
  def setTilePosition(tileX:Int, tileY:Int, value:Int) {
    _set(tileX, tileY, value)
  }
  def set(position:Position, value:Int) {
    setPosition(position.getX, position.getY, value)
  }
  def set(position:WalkPosition, value:Int) {
    setWalkPosition(position.getX, position.getY, value)
  }
  def set(position:TilePosition, value:Int) {
    setTilePosition(position.getX, position.getY, value)
  }
  def add(i:Int, value:Int) {
    if (i < 0 || i >= _positions.length) return
    _positions(i) += value
  }
  def _add(x:Int, y:Int, value:Int) {
    add(x + y * _width, value)
  }
  def addPosition(x:Int, y:Int, value:Int) {
    _add(_pointsPerTile*x/32, _pointsPerTile*y/32, value)
  }
  def addWalkPosition(walkX:Int, walkY:Int, value:Int) {
    _add(_pointsPerTile*walkX/4, _pointsPerTile*walkY/4, value)
  }
  def addTilePosition(tileX:Int, tileY:Int, value:Int) {
    _add(tileX, tileY, value)
  }
  def add(position:Position, value:Int) {
    addPosition(position.getX, position.getY, value)
  }
  def add(position:WalkPosition, value:Int) {
    addWalkPosition(position.getX, position.getY, value)
  }
  def add(position:TilePosition, value:Int) {
    addTilePosition(position.getX, position.getY, value)
  }
}
