package Mathematics.Shapes

import Mathematics.Points.Point

object Ring {

  private def pointsForRadius(radius: Int): IndexedSeq[Point] = {
    if (radius == 0) return Vector(Point(0, 0))
    val rOut = radius * radius
    val rIn = if (radius < 1) 0 else (radius - 1) * (radius - 1)
    (-radius to radius).flatten(x =>
      (-radius to radius).map(y =>
        (x, y, { val d = x * x + y * y; d <= rOut && d > rIn })))
      .filter(_._3)
      .map(point => Point(point._1, point._2))
 }

  private val _cachedPointMax = 50
  private val _cachedPoints = (0 to _cachedPointMax).map(pointsForRadius).toArray

  def apply(radius: Int): IndexedSeq[Point] = if (radius <= _cachedPointMax) _cachedPoints(Math.max(0, radius)) else pointsForRadius(radius)
}
