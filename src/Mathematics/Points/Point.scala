package Mathematics.Pixels

import Mathematics.Points.AbstractPoint

case class Point(argX:Int, argY:Int) extends AbstractPoint(argX, argY) {
  def position:Pixel = new Pixel(x, y)
  def tile:Tile = new Tile(x, y)
}
