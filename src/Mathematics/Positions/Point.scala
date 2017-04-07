package Mathematics.Positions

import bwapi.{Position, TilePosition}

case class Point(val x:Int, val y:Int) {
  def position:Position = new Position(x, y)
  def tile:TilePosition = new TilePosition(x, y)
  def length:Double = Math.sqrt(lengthSquared)
  def lengthSquared:Double = x*x+y*y
  override def toString:String = "[" + x + ", " + y + "]"
}
