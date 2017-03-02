package Geometry.Grids.Abstract

import bwapi.{Position, TilePosition, WalkPosition}

class GridInt extends Grid[Int] {
  
  override val _positions:Array[Int] = Array.fill(_width * _height)(_defaultValue)
  override def _defaultValue:Int = 0
  
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
  
  override def repr(value: Int) = value.toString
}
