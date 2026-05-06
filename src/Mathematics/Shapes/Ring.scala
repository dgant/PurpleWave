package Mathematics.Shapes

import Mathematics.Points.Point
import Utilities.?

object Ring {

  @volatile private var cache = Array.empty[IndexedSeq[Point]]

  def apply(radius: Int): IndexedSeq[Point] = {
    if (radius < 0) return Vector.empty
    val current = cache
    if (radius < current.length) return current(radius)
    this.synchronized {
      if (radius >= cache.length) {
        val updated = cache.toBuffer
        while (updated.length <= radius) updated += build(updated.length)
        cache = updated.toArray
      }
      cache(radius)
    }
  }

  private def build(radius: Int): IndexedSeq[Point] = {
    if (radius == 0) return Vector(Point(0, 0))
    val rOut = radius * radius
    val rIn = ?(radius < 1, 0, (radius - 1) * (radius - 1))
    val output = Vector.newBuilder[Point]
    var x = -radius
    while (x <= radius) {
      var y = -radius
      while (y <= radius) {
        val d = x * x + y * y
        if (d <= rOut && d > rIn) {
          output += Point(x, y)
        }
        y += 1
      }
      x += 1
    }
    output.result()
  }
}
