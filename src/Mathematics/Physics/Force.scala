package Mathematics.Physics

import Mathematics.Points.{AbstractPoint, Point}
import Mathematics.PurpleMath

case class Force(x: Double, y: Double) {
  
  def this() = this(0.0, 0.0)
  def this(point: AbstractPoint) = this(point.x, point.y)
  
  @inline def unary_- = Force(-x, -y)
  @inline def +(other: Force)   : Force = Force(x + other.x, y + other.y)
  @inline def -(other: Force)   : Force = Force(x - other.x, y - other.y)
  @inline def *(other: Force)   : Double = x * other.x + y * other.y
  @inline def *(value: Double)  : Force = Force(x * value, y * value)
  @inline def /(value: Double)  : Force = Force(x / value, y / value)
  
  @inline def radians: Double = PurpleMath.atan2(y, x)
  @inline def lengthSquared: Double = x * x + y * y
  @inline def lengthSlow: Double = Math.sqrt(lengthSquared)
  @inline def lengthFast: Double = PurpleMath.broodWarDistanceDouble(0.0, 0.0, x, y)

  @inline def normalize: Force = normalize(1.0)
  @inline def normalize(scale: Double = 1.0): Force = {
    val length = Math.sqrt(x*x+y*y)
    if (length == 0)
      Force(scale.toInt, 0)
    else
      Force(
        scale * x / length,
        scale * y / length)
  }
  
  @inline def clipMin(scale: Double): Force = {
    if (scale * scale >= lengthSquared) this else normalize(scale)
  }
  
  @inline def clipMax(scale: Double): Force = {
    if (scale * scale <= lengthSquared) this else normalize(scale)
  }
  
  @inline def toPoint: Point = Point(x.toInt, y.toInt)
}


