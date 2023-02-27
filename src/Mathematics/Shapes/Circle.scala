package Mathematics.Shapes

import Mathematics.Points.Point

object Circle {

  def apply(radius: Int): Seq[Point] =
    (-radius to radius).view.flatMap(x =>
      (-radius to radius).view.map(y =>
        (x, y, x * x + y * y <= radius * radius)))
      .filter(_._3)
      .map(point => Point(point._1, point._2))
}
