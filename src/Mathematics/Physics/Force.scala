package Mathematics.Physics

import Mathematics.Points.{AbstractPoint, Point}

case class Force(x: Double, y: Double) {
  
  def this(point: AbstractPoint) = this(point.x, point.y)
  
  def unary_- = Force(-x, -y)
  def +(other: Force): Force = Force(x + other.x, y + other.y)
  def -(other: Force): Force = Force(x - other.x, y - other.y)
  
  def normalize(scale: Double): Point = {
    val length = Math.sqrt(x*x+y*y)
    if (length == 0)
      Point(scale.toInt, 0)
    else
      Point((scale*x/length).toInt, (scale*y/length).toInt)
  }
}
