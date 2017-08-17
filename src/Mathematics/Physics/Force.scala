package Mathematics.Physics

import Mathematics.Points.{AbstractPoint, Point}

case class Force(x: Double, y: Double) {
  
  def this() = this(0.0, 0.0)
  def this(point: AbstractPoint) = this(point.x, point.y)
  
  def unary_- = Force(-x, -y)
  def +(other: Force): Force = Force(x + other.x, y + other.y)
  def -(other: Force): Force = Force(x - other.x, y - other.y)
  
  def normalize: Force = normalize(1.0)
  
  def normalize(scale: Double): Force = {
    val length = Math.sqrt(x*x+y*y)
    if (length == 0)
      Force(scale.toInt, 0)
    else
      Force(
        (scale*x/length).toInt,
        (scale*y/length).toInt)
  }
  
  def toPoint: Point = Point(x.toInt, y.toInt)
}


