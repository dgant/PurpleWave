package Mathematics.Points

abstract class AbstractPoint(val x: Int, val y: Int) {
  
  @inline final def length        : Double = Math.sqrt(lengthSquared)
  @inline final def lengthSquared : Double = x * x + y * y
  
  @inline final def pixel : Pixel = Pixel(x, y)
  @inline final def tile  : Tile  = Tile(x, y)
  
  override def toString: String = "[" + x + ", " + y + "]"
}
