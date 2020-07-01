package Mathematics.Points

import Mathematics.PurpleMath

abstract class AbstractPoint(val x: Int, val y: Int) {
  
  @inline final def length        : Double = Math.sqrt(lengthSquared)
  @inline final def lengthSquared : Double = x * x + y * y
  
  @inline final def pixel : Pixel = Pixel(x, y)
  @inline final def tile  : Tile  = Tile(x, y)

  def direction: Point =
    if (Math.abs(x) > Math.abs(y))
      Point(PurpleMath.signum(x), 0)
    else
      Point(0, PurpleMath.signum(y))

  def maxDimensionLength: Int = Math.max(Math.abs(x), Math.abs(y))
  
  override def toString: String = "[" + x + ", " + y + "]"
}
