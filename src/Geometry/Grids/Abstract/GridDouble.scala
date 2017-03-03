package Geometry.Grids.Abstract

import bwapi.{Position, TilePosition, WalkPosition}

class GridDouble extends Grid[Double] {
  
  override val _positions: Array[Double] = Array.fill(_width * _height)(_defaultValue)
  override def _defaultValue:Double = 0d
  override def repr(value: Double) = (value * 10).toInt.toString
  
  def add(i:Int, value:Double) {
    if (i < 0 || i >= _positions.length) return
    _positions(i) += value
  }
  def _add(x:Int, y:Int, value:Double) {
    add(x + y * _width, value)
  }
  def addPosition(x:Int, y:Int, value:Double) {
    _add(_pointsPerTile*x/32, _pointsPerTile*y/32, value)
  }
  def addWalkPosition(walkX:Int, walkY:Int, value:Double) {
    _add(_pointsPerTile*walkX/4, _pointsPerTile*walkY/4, value)
  }
  def addTilePosition(tileX:Int, tileY:Int, value:Double) {
    _add(tileX, tileY, value)
  }
  def add(position:Position, value:Double) {
    addPosition(position.getX, position.getY, value)
  }
  def add(position:WalkPosition, value:Double) {
    addWalkPosition(position.getX, position.getY, value)
  }
  def add(position:TilePosition, value:Double) {
    addTilePosition(position.getX, position.getY, value)
  }
}
