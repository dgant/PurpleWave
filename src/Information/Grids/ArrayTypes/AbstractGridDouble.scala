package Information.Grids.ArrayTypes

import java.text.DecimalFormat

import bwapi.TilePosition

class AbstractGridDouble extends AbstractGridArray[Double] {
  
  override protected var values: Array[Double] = Array.fill(width * height)(defaultValue)
  override def defaultValue:Double = 0.0
  override def repr(value: Double):String = formatter.format(value)
  
  val formatter = new DecimalFormat("#.##")
  
  def add(i:Int, value:Double):Unit                = if (valid(i)) values(i) += value
  def add(tileX:Int, tileY:Int, value:Double):Unit = add(i(tileX, tileY), value)
  def add(tile:TilePosition, value:Double):Unit    = add(i(tile.getX, tile.getY), value)
}
