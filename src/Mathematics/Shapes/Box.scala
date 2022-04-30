package Mathematics.Shapes

import Mathematics.Points.Point

object Box {
  def apply(width: Int, height: Int): IndexedSeq[Point] = (
        (0          until width)  .map(x => Point(x,          0))
    ++  (1          until height) .map(y => Point(width - 1,  y))
    ++  (width - 1  to    0)      .map(x => Point(x,          height - 1))
    ++  (height - 1 until 0)      .map(y => Point(0,          y)))
}
