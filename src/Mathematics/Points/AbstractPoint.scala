package Mathematics.Points

abstract class AbstractPoint(val x: Int, val y: Int) {
  
  @inline final def length        : Double = Math.sqrt(lengthSquared)
  @inline final def lengthSquared : Double = x * x + y * y

  def direction: Direction = new Direction(this)

  protected final val radiansOverDegrees = 2.0 * Math.PI / 256.0

  @inline final def asPixel: Pixel = Pixel(x, y)
  @inline final def asTile: Tile = Tile(x, y)
  @inline final def asPoint: Point = Point(x, y)
  
  override def toString: String = "[" + x + ", " + y + "]"
}
