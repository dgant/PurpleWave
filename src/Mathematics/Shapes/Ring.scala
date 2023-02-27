package Mathematics.Shapes

import Mathematics.Points.Point
import Utilities.?

object Ring {

  def apply(radius: Int): Seq[Point] = {
    if (radius == 0) return Vector(Point(0, 0))
    val rOut = radius * radius
    val rIn = ?(radius < 1, 0, (radius - 1) * (radius - 1))
    (-radius to radius).view.flatMap(x =>
      (-radius to radius).view.map(y =>
        (x, y, { val d = x * x + y * y; d <= rOut && d > rIn })))
      .filter(_._3)
      .map(point => Point(point._1, point._2))
 }
}
