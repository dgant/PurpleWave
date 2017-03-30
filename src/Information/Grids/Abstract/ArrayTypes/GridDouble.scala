package Information.Grids.Abstract.ArrayTypes

import java.text.DecimalFormat

import bwapi.TilePosition

class GridDouble extends GridArray[Double] {
  
  override protected var values: Array[Double] = Array.fill(width * height)(defaultValue)
  override def defaultValue:Double = 0d
  override def repr(value: Double):String = formatter.format(value)
  
  val formatter = new DecimalFormat("#.##")
  
  def add(i:Int, value:Double):Unit                = if (valid(i)) values(i) += value
  def add(tileX:Int, tileY:Int, value:Double):Unit = add(i(tileX, tileY), value)
  def add(tile:TilePosition, value:Double):Unit    = add(i(tile.getX, tile.getY), value)
}
