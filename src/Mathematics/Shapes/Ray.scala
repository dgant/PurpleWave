package Mathematics.Shapes

import Mathematics.Maff
import Mathematics.Points.{Pixel, Tile}

object Ray {

  @inline final def apply(from: Pixel, lengthPixels: Double, radians: Double): Iterator[Pixel] = {
    apply(from, from.radiateRadians(radians, lengthPixels))
  }

  case class Generator(from: Pixel, to: Pixel) extends Iterator[Pixel] {
    val signX             = Maff.signum(to.x - from.x)
    val signY             = Maff.signum(to.y - from.y)
    val dx                = to.x - from.x
    val dy                = to.y - from.y
    val velocityX         = dx.toDouble
    val velocityY         = dy.toDouble
    val tileCount         = 1 + from.tile.tileDistanceManhattan(to.tile)
    val output            = new Array[Tile](tileCount)
    val lengthSquaredGoal = dx * dx + dy * dy
    var lengthSquaredNow  = 0
    var x                 = from.x
    var y                 = from.y

    var cornerSkip: Option[Pixel] = None

    override def hasNext: Boolean = cornerSkip.isEmpty && lengthSquaredNow < lengthSquaredGoal - 0.0001

    override def next(): Pixel = {
      if (cornerSkip.isDefined) {
        val output = cornerSkip.get
        cornerSkip = None
        return output
      }
      val output = Pixel(x, y)
      if (hasNext) {
        val nextXCrossing   = x + signX * Math.abs(32 - Math.abs(x) % 32)
        val nextYCrossing   = y + signY * Math.abs(32 - Math.abs(y) % 32)
        val timeToNextX     = (nextXCrossing - x) / velocityX
        val timeToNextY     = (nextYCrossing - y) / velocityY
        val deltaTime       = Math.min(timeToNextX, timeToNextY)
        x                   = Math.ceil(x + velocityX * deltaTime).toInt
        y                   = Math.ceil(y + velocityY * deltaTime).toInt
        lengthSquaredNow    = Maff.squared(x - from.x) + Maff.squared(y - from.y)
        if (x % 32 == 0 && y % 32 == 0) {
          // We landed right on the corner of the grid, which would cause us to skip a corner tile.
          cornerSkip = Some(Pixel(x, y - signY))
        }
      }
      output
    }
  }
  
  @inline final def apply(from: Pixel, to: Pixel): Iterator[Pixel] = {
    if (to.x == from.x) {
      if (to.y == from.y) return Iterator(to)
      return (from.y / 32 to to.y / 32 by Maff.signum(to.y - from.y)).iterator.map(tileY => Pixel(to.x, tileY * 32))
    } else  if (to.y == from.y) {
      return (from.x / 32 to to.x / 32 by Maff.signum(to.x - from.x)).iterator.map(tileX => Pixel(tileX * 32, to.y))
    }
    Generator(from, to)
  }
}
