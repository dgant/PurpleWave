package Mathematics.Physics

import Mathematics.Points.AbstractPoint

case class Force(argX: Int, argY: Int) extends AbstractPoint(argX, argY) {
  
  def this(point: AbstractPoint) = this(point.x, point.y)
  
  def unary_- = Force(-x, -y)
  def +(other: Force): Force = Force(x + other.x, y + other.y)
  def -(other: Force): Force = Force(x - other.x, y - other.y)
  
}
