package Mathematics.Points

import Mathematics.PurpleMath

case class Direction(override val x: Int, override val y: Int) extends AbstractPoint (
  if (x*x < y*y) 0 else PurpleMath.signum(x),
  if (x*x >= y*y) 0 else PurpleMath.signum(y)) {

  def this(start: AbstractPoint, end: AbstractPoint) {
    this(end.x - start.x, end.y - start.y)
  }
  def isVertical: Boolean = x == 0
  def isHorizontal: Boolean = y == 0
  def isUp: Boolean = y < 0
  def isDown: Boolean = y > 0
  def isLeft: Boolean = x < 0
  def isRight: Boolean = x > 0
}
