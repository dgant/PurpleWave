package Mathematics.Points

import Mathematics.PurpleMath

case class Point(argX: Int, argY: Int) extends AbstractPoint(argX, argY) {
  @inline final def degreesTo(other: Point): Double = {
    radiansTo(other) / radiansOverDegrees
  }
  @inline final def radiansTo(other: Point): Double = {
    PurpleMath.atan2(other.y - y, other.x - x)
  }
  @inline final def distanceSquared(other: Point): Double = {
    val dx = x - pixel.x
    val dy = y - pixel.y
    dx * dx + dy * dy
  }
}