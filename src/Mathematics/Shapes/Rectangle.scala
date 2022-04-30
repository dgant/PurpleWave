package Mathematics.Shapes

import Mathematics.Points.Point

object Rectangle {
  def apply(width: Int, height: Int): IndexedSeq[Point] =
    (0 until height).flatten(dy =>
      (0 until width).map(dx =>
        Point(dx, dy)))
}
