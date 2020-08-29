package Mathematics.Points

import Mathematics.PurpleMath

class Direction(x: Int, y: Int) extends AbstractPoint(
  if (x*x < y*y) 0 else PurpleMath.signum(x),
  if (x*x >= y*y) 0 else PurpleMath.signum(y)) {

  def this(start: AbstractPoint, end: AbstractPoint) {
    this(end.x - start.x, end.y - start.y)
  }
}
