package Mathematics.Physics

import Mathematics.Points.{AbstractPoint, Point}

case class Force(x: Double, y: Double) {
  
  def this() = this(0.0, 0.0)
  def this(point: AbstractPoint) = this(point.x, point.y)
  
  def unary_- = Force(-x, -y)
  def +(other: Force): Force = Force(x + other.x, y + other.y)
  def -(other: Force): Force = Force(x - other.x, y - other.y)
  
  def lengthSquared: Double = x * x + y * y
  def lengthSlow: Double  = Math.sqrt(lengthSquared)
  def lengthFast: Double = {
    // Octagonal distance
    // https://en.wikibooks.org/wiki/Algorithms/Distance_approximations#Octagonal
    val ax = Math.abs(x)
    val ay = Math.abs(y)
    0.941256 * Math.max(ax, ay) + Math.min(ax, ay) * 0.414213562
  }
  
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
  
  def toPoint: Point = Point(x.toInt, y.toInt)
}


