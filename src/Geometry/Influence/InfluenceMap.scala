package Geometry.Influence

import Startup.With
import bwapi.{Position, TilePosition, WalkPosition}

abstract class InfluenceMap {
  
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
  def get(i:Int):Int = {
    _positions(i)
  }
  def get(x:Int, y:Int):Int = {
    get(x + y * _width)
  }
  def get(position:Position):Int = {
    get(_pointsPerTile*position.getX/32, _pointsPerTile*position.getY/32)
  }
  def get(position:WalkPosition):Int = {
    get(_pointsPerTile*position.getX/8, _pointsPerTile*position.getY/8)
  }
  def get(position:TilePosition):Int = {
    get(_pointsPerTile*position.getX, _pointsPerTile*position.getY)
  }
  def set(i:Int, value:Int) {
    _positions(i) = value
  }
  def set(x:Int, y:Int, value:Int) {
    set(x + y * _width, value)
  }
  def set(position:Position, value:Int) {
    set(_pointsPerTile*position.getX/32, _pointsPerTile*position.getY/32, value)
  }
  def set(position:WalkPosition, value:Int) {
    set(_pointsPerTile*position.getX/8, _pointsPerTile*position.getY/8, value)
  }
  def set(position:TilePosition, value:Int) {
    set(_pointsPerTile*position.getX, _pointsPerTile*position.getY, value)
  }
  def add(i:Int, value:Int) {
    _positions(i) += value
  }
  def add(x:Int, y:Int, value:Int) {
    add(x + y * _width, value)
  }
  def add(position:Position, value:Int) {
    add(_pointsPerTile*position.getX/32, _pointsPerTile*position.getY/32, value)
  }
  def add(position:WalkPosition, value:Int) {
    add(_pointsPerTile*position.getX/8, _pointsPerTile*position.getY/8, value)
  }
  def add(position:TilePosition, value:Int) {
    add(_pointsPerTile*position.getX, _pointsPerTile*position.getY, value)
  }
}
