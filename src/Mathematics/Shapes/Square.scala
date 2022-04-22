package Mathematics.Shapes

import Mathematics.Points.Point

object Square {
  def apply(count: Int): IndexedSeq[Point] =
    (0 until count).flatten(dy =>
      (0 until count).map(dx =>
        Point(dx, dy)))
}
