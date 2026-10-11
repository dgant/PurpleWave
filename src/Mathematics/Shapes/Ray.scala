package Mathematics.Shapes

import Mathematics.Maff
import Mathematics.Points.Pixel

object Ray {

  @inline final def apply(from: Pixel, lengthPixels: Double, radians: Double): Iterator[Pixel] = {
    apply(from, from.radiateRadians(radians, lengthPixels).clamp())
  }

  case class Generator(from: Pixel, to: Pixel) extends Iterator[Pixel] {
    private val dx = to.x - from.x
    private val dy = to.y - from.y
    private val signX = Maff.signum101(dx)
    private val signY = Maff.signum101(dy)
    private var tileX = Maff.div32(from.x)
    private var tileY = Maff.div32(from.y)
    private val deltaX = if (dx == 0) Double.PositiveInfinity else 32.0 / Math.abs(dx)
    private val deltaY = if (dy == 0) Double.PositiveInfinity else 32.0 / Math.abs(dy)
    private var nextX = if (dx == 0) Double.PositiveInfinity else
      (32 * (tileX + (if (dx > 0) 1 else 0)) - from.x).toDouble / dx
    private var nextY = if (dy == 0) Double.PositiveInfinity else
      (32 * (tileY + (if (dy > 0) 1 else 0)) - from.y).toDouble / dy
    private var pending = List(from)
    private var finished = from == to

    private def inTile(time: Double, x: Int, y: Int): Pixel = Pixel(
      Maff.clamp(Math.floor(from.x + time * dx).toInt, 32 * x, 32 * x + 31),
      Maff.clamp(Math.floor(from.y + time * dy).toInt, 32 * y, 32 * y + 31))

    override def hasNext: Boolean = pending.nonEmpty

    override def next(): Pixel = {
      if (!hasNext) throw new NoSuchElementException("Ray exhausted")
      val output = pending.head
      pending = pending.tail
      if (pending.isEmpty && !finished) {
        val time = Math.min(nextX, nextY)
        if (time >= 1) {
          pending = List(to)
          finished = true
        } else if (Math.abs(nextX - nextY) < 1e-12) {
          // At a corner, include both side tiles as well as the diagonal tile.
          pending = List(inTile(time, tileX + signX, tileY), inTile(time, tileX, tileY + signY))
          tileX += signX
          tileY += signY
          pending :+= inTile(time, tileX, tileY)
          nextX += deltaX
          nextY += deltaY
        } else if (nextX < nextY) {
          tileX += signX
          pending = List(inTile(time, tileX, tileY))
          nextX += deltaX
        } else {
          tileY += signY
          pending = List(inTile(time, tileX, tileY))
          nextY += deltaY
        }
      }
      output
    }
  }
  
  @inline final def apply(from: Pixel, to: Pixel): Iterator[Pixel] = Generator(from, to)
}
