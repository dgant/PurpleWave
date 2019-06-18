package Mathematics.Points

import Mathematics.PurpleMath

abstract class AbstractPoint(val x: Int, val y: Int) {
  
  def length        : Double = Math.sqrt(lengthSquared)
  def lengthSquared : Double = x * x + y * y
  
  def pixel : Pixel = Pixel(x, y)
  def tile  : Tile  = Tile(x, y)

  def direction: Point =
    if (Math.abs(x) > Math.abs(y))
      Point(PurpleMath.signum(x), 0)
    else
      Point(0, PurpleMath.signum(y))

  def maxDimensionLength: Int = Math.max(Math.abs(x), Math.abs(y))
  
  override def toString: String = "[" + x + ", " + y + "]"
}
