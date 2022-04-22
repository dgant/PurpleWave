package Mathematics.Shapes

import Mathematics.Points.Point

object Circle {

  private def pointsForRadius(radius: Int): IndexedSeq[Point] =
    (-radius to radius).flatten(x =>
      (-radius to radius).map(y =>
        (x, y, x * x + y * y <= radius * radius)))
      .filter(_._3)
      .map(point => Point(point._1, point._2))

  private val _cachedPointMax = 50
  private val _cachedPoints = (0 to _cachedPointMax).map(pointsForRadius(_).toVector).toVector

  def apply(radius: Int): IndexedSeq[Point] = if (radius <= _cachedPointMax) _cachedPoints(Math.max(0, radius)) else pointsForRadius(radius)
}
