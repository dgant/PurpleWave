package Mathematics.Shapes

import Mathematics.Points.Point

object Box {
  def apply(width: Int, height: Int): IndexedSeq[Point] = (
        (0          until width  - 1  by  1).map(x => Point(x,          0))
    ++  (0          until height - 1  by  1).map(y => Point(width - 1,  y))
    ++  (width - 1  until 0           by -1).map(x => Point(x,          height - 1))
    ++  (height - 1 until 0           by -1).map(y => Point(0,          y)))
}
