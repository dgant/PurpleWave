package Mathematics.Points

import Mathematics.PurpleMath

class Direction(x: Int, y: Int) extends Point(PurpleMath.signum(x), PurpleMath.signum(y)) {
  def rotateA: Direction = new Direction(y, -x)
  def rotateB: Direction = new Direction(y, x)
  object Up extends Direction(0, -1)
  object Down extends Direction(0, 1)
  object Left extends Direction(-1, 0)
  object Right extends Direction(1, 0)
}
