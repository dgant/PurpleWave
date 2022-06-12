package Mathematics.Shapes

import Mathematics.Points.Point

object Corners {
  def apply(width: Int, height: Int): IndexedSeq[Point] = Vector(
    Point(0,         0),
    Point(width - 1, 0),
    Point(width - 1, height - 1),
    Point(0,         height - 1))
}
