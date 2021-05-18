package Information.Grids.ArrayTypes

import Mathematics.Points.Tile

class AbstractGridInt extends AbstractGridArray[Int] {

  override val defaultValue: Int = 0
  override protected var values: Array[Int] = Array.fill(length)(defaultValue)
  override def repr(value: Int): String = value.toString
  
  def add(i: Int,                 value: Int) { if (valid(i)) values(i) += value }
  def add(tileX: Int, tileY: Int, value: Int) { add(i(tileX, tileY), value) }
  def add(tile: Tile,             value: Int) { add(i(tile.x, tile.y), value) }
}
