package Mathematics.Physics

import Mathematics.Points.{AbstractPoint, Point}
import Mathematics.PurpleMath

case class Force(x: Double, y: Double) {
  
  def this() = this(0.0, 0.0)
  def this(point: AbstractPoint) = this(point.x, point.y)
  
  def unary_- = Force(-x, -y)
  def +(other: Force)   : Force = Force(x + other.x, y + other.y)
  def -(other: Force)   : Force = Force(x - other.x, y - other.y)
  def *(other: Force)   : Double = x * other.x + y * other.y
  def *(value: Double)  : Force = Force(value * x, value * y)
  def /(value: Double)  : Force = Force(value / x, value / y)
  
  lazy val radians: Double = PurpleMath.atan2(y, x)
  lazy val lengthSquared: Double = x * x + y * y
  lazy val lengthSlow: Double = Math.sqrt(lengthSquared)
  lazy val lengthFast: Double = PurpleMath.broodWarDistanceDouble(0.0, 0.0, x, y)
  
  def normalize: Force = normalize(1.0)
  def normalize(scale: Double): Force = {
    val length = Math.sqrt(x*x+y*y)
    if (length == 0)
      Force(scale.toInt, 0)
    else
      Force(
        scale * x / length,
        scale * y / length)
  }
  
  def clipMin(scale: Double): Force = {
    if (scale * scale >= lengthSquared) this else normalize(scale)
  }
  
  def clipMax(scale: Double): Force = {
    if (scale * scale <= lengthSquared) this else normalize(scale)
  }
  
  def toPoint: Point = Point(x.toInt, y.toInt)
}


