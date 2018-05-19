package Mathematics.Points

abstract class AbstractPoint(val x: Int, val y: Int) {
  
  def length        : Double = Math.sqrt(lengthSquared)
  def lengthSquared : Double = x * x + y * y
  
  def pixel : Pixel = Pixel(x, y)
  def tile  : Tile  = Tile(x, y)
  
  override def toString: String = "[" + x + ", " + y + "]"
}
