package Information.Grids.ArrayTypes

import Mathematics.Points.Tile

class AbstractGridInt extends AbstractGridArray[Int] {
  
  override protected var values: Array[Int] = Array.fill(width * height)(defaultValue)
  override def defaultValue:Int = 0
  override def repr(value: Int) = value.toString
  
  def add(i:Int, value:Int):Unit                = if (valid(i)) values(i) += value
  def add(tileX:Int, tileY:Int, value:Int):Unit = add(i(tileX, tileY), value)
  def add(tile:Tile, value:Int):Unit    = add(i(tile.x, tile.y), value)
}
