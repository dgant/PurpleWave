package Information.Grids.ArrayTypes

import bwapi.TilePosition

class AbstractGridInt extends AbstractGridArray[Int] {
  
  override protected var values: Array[Int] = Array.fill(width * height)(defaultValue)
  override def defaultValue:Int = 0
  override def repr(value: Int) = value.toString
  
  def add(i:Int, value:Int):Unit                = if (valid(i)) values(i) += value
  def add(tileX:Int, tileY:Int, value:Int):Unit = add(i(tileX, tileY), value)
  def add(tile:TilePosition, value:Int):Unit    = add(i(tile.getX, tile.getY), value)
}
