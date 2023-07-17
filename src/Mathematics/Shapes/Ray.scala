package Mathematics.Shapes

import Mathematics.Maff
import Mathematics.Points.Pixel

object Ray {

  @inline final def apply(from: Pixel, lengthPixels: Double, radians: Double): Iterator[Pixel] = {
    apply(from, from.radiateRadians(radians, lengthPixels).clamp())
  }

  case class Generator(from: Pixel, to: Pixel) extends Iterator[Pixel] {
    val signX             = Maff.signum101(to.x - from.x)
    val signY             = Maff.signum101(to.y - from.y)
    val dx                = to.x - from.x
    val dy                = to.y - from.y
    val velocityX         = dx.toDouble
    val velocityY         = dy.toDouble
    val invVelocityX      = 1.0 / velocityX
    val invVelocityY      = 1.0 / velocityY
    val tileCount         = 1 + from.tile.tileDistanceManhattan(to.tile)
    val lengthSquaredGoal = dx * dx + dy * dy
    var lengthSquaredNow  = 0
    var x                 = from.x
    var y                 = from.y
    var at                = from

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
        val nextXCrossing   = x + signX * Math.abs(32 - Maff.mod32(Math.abs(x)))
        val nextYCrossing   = y + signY * Math.abs(32 - Maff.mod32(Math.abs(y)))
        val timeToNextX     = (nextXCrossing - x) * invVelocityX
        val timeToNextY     = (nextYCrossing - y) * invVelocityY
        val deltaTime       = Math.min(timeToNextX, timeToNextY)
        x                   = Math.ceil(x + velocityX * deltaTime).toInt
        y                   = Math.ceil(y + velocityY * deltaTime).toInt
        lengthSquaredNow    = Maff.squared(x - from.x) + Maff.squared(y - from.y)
        if (Maff.mod32(x) == 0 && Maff.mod32(y) == 0) {
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
      return (Maff.div32(from.y) to Maff.div32(to.y) by Maff.signum101(to.y - from.y)).iterator.map(tileY => Pixel(to.x, Maff.x32(tileY)))
    } else  if (to.y == from.y) {
      return (Maff.div32(from.x) to Maff.div32(to.x) by Maff.signum101(to.x - from.x)).iterator.map(tileX => Pixel(Maff.x32(tileX), to.y))
    }
    Generator(from, to)
  }
}
