package Geometry.Grids.Abstract

import bwapi.TilePosition

class GridInt extends Grid[Int] {
  
  override val _positions: Array[Int] = Array.fill(_width * _height)(_defaultValue)
  override def _defaultValue:Int = 0
  override def repr(value: Int) = value.toString
  
  def add(i:Int, value:Int):Unit                = if (valid(i)) _positions(i) += value
  def add(tileX:Int, tileY:Int, value:Int):Unit = add(i(tileX, tileY), value)
  def add(tile:TilePosition, value:Int):Unit    = add(i(tile.getX, tile.getY), value)
}
