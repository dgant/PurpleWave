package Mathematics.Shapes

import Mathematics.Points.Point

object RoundedBox {
  def apply(width: Int, height: Int): IndexedSeq[Point] = (
        (1          until width  - 1  by  1).map(x => Point(x,          0))
    ++  (1          until height - 1  by  1).map(y => Point(width - 1,  y))
    ++  (width - 2  until 0           by -1).map(x => Point(x,          height - 1))
    ++  (height - 2 until 0           by -1).map(y => Point(0,          y)))
}
