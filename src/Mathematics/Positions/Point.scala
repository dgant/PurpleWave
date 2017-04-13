package Mathematics.Pixels

case class Point(val x:Int, val y:Int) {
  def position:Pixel = new Pixel(x, y)
  def tile:Tile = new Tile(x, y)
  def length:Double = Math.sqrt(lengthSquared)
  def lengthSquared:Double = x*x+y*y
  override def toString:String = "[" + x + ", " + y + "]"
}
