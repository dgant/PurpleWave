package Mathematics.Points

import Mathematics.Maff

class Direction(x: Int, y: Int) extends AbstractPoint (
  if (x*x < y*y) 0 else Maff.signum(x),
  if (x*x >= y*y) 0 else Maff.signum(y)) {

  def this(start: AbstractPoint, end: AbstractPoint) {
    this(end.x - start.x, end.y - start.y)
  }

  def this(source: AbstractPoint) {
    this(source.x, source.y)
  }

  def isVertical: Boolean = x == 0
  def isHorizontal: Boolean = y == 0
  def isUp: Boolean = y < 0
  def isDown: Boolean = y > 0
  def isLeft: Boolean = x < 0
  def isRight: Boolean = x > 0
}
