package Mathematics.Shapes

import Mathematics.Points.Point

object Square {
  def apply(width: Int): IndexedSeq[Point] = Rectangle(width, width)
}
