package Mathematics.Points

import Mathematics.Maff

class Direction(_x: Int, _y: Int) extends AbstractPoint (
  if (_x*_x < _y*_y) 0 else Maff.signum(_x),
  if (_x*_x >= _y*_y) 0 else Maff.signum(_y)) {

  def this(start: AbstractPoint, end: AbstractPoint) {
    this(end.x - start.x, end.y - start.y)
  }

  def this(source: AbstractPoint) {
    this(source.x, source.y)
  }

  @inline final def isVertical: Boolean = x == 0
  @inline final def isHorizontal: Boolean = y == 0
  @inline final def isUp: Boolean = y < 0
  @inline final def isDown: Boolean = y > 0
  @inline final def isLeft: Boolean = x < 0
  @inline final def isRight: Boolean = x > 0

  @inline final override def equals(other: Any): Boolean = {
    other.isInstanceOf[Direction] && other.asInstanceOf[Direction].x == x && other.asInstanceOf[Direction].y == y
  }
}
