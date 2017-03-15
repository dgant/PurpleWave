package Information.Grids.Abstract

import java.text.DecimalFormat

import bwapi.TilePosition

class GridDouble extends GridArray[Double] {
  
  override val _positions: Array[Double] = Array.fill(_width * _height)(defaultValue)
  override def defaultValue:Double = 0d
  
  val formatter = new DecimalFormat("#.##")
  override def repr(value: Double):String = formatter.format(value)
  
  def add(i:Int, value:Double):Unit                = if (valid(i)) _positions(i) += value
  def add(tileX:Int, tileY:Int, value:Double):Unit = add(i(tileX, tileY), value)
  def add(tile:TilePosition, value:Double):Unit    = add(i(tile.getX, tile.getY), value)
}
