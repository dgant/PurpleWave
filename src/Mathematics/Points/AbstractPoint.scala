package Mathematics.Points

abstract class AbstractPoint(val x: Int, val y: Int) {
  
  @inline final def length        : Double = Math.sqrt(lengthSquared)
  @inline final def lengthSquared : Double = x * x + y * y
  
  @inline final def pixel : Pixel = Pixel(x, y)
  @inline final def tile  : Tile  = Tile(x, y)

  def direction: Direction = new Direction(this)

  @inline final def maxDimensionLength: Int = Math.max(Math.abs(x), Math.abs(y))

  protected val radiansOverDegrees = 2.0 * Math.PI / 256.0
  
  override def toString: String = "[" + x + ", " + y + "]"
}
