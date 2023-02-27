package Mathematics.Shapes

import Mathematics.Maff
import Mathematics.Points.{Pixel, Tile}

object Ray {

  @inline final def apply(from: Pixel, lengthPixels: Double, radians: Double): Iterator[Tile] = {
    apply(from, from.radiateRadians(radians, lengthPixels))
  }

  case class Generator(from: Pixel, to: Pixel) extends Iterator[Tile] {
    val gridSize    = 32
    val signX       = Maff.signum(to.x - from.x)
    val signY       = Maff.signum(to.y - from.y)
    var x           = from.x
    var y           = from.y
    val velocityX   = to.x - from.x.toDouble
    val velocityY   = to.y - from.y.toDouble
    val tileCount   = 1 + from.tile.tileDistanceManhattan(to.tile)
    val output      = new Array[Tile](tileCount)
    var tileIndex   = 0
    var cornerSkip: Option[Tile] = None

    override def hasNext: Boolean = tileIndex < tileCount

    override def next(): Tile = {
      if (cornerSkip.isDefined) {
        val output = cornerSkip.get
        cornerSkip = None
        return output
      }
      val output = Tile(x / 32, y / 32)
      tileIndex  += 1
      if (tileIndex < tileCount) {
        val nextXCrossing   = x + signX * Math.abs(gridSize - Math.abs(x) % gridSize)
        val nextYCrossing   = y + signY * Math.abs(gridSize - Math.abs(y) % gridSize)
        val timeToNextX     = (nextXCrossing - x) / velocityX
        val timeToNextY     = (nextYCrossing - y) / velocityY
        val deltaTime       = Math.min(timeToNextX, timeToNextY)
        x                   = Math.ceil(x + velocityX * deltaTime).toInt
        y                   = Math.ceil(y + velocityY * deltaTime).toInt
        if (x % gridSize == 0 && y % gridSize == 0) {
          // We landed right on the corner of the grid, which would cause us to skip a corner tile.
          cornerSkip = Some(Tile(x / 32, y / 32 - signY))
          tileIndex += 1
        }
      }
      output
    }
  }
  
  @inline final def apply(from: Pixel, to: Pixel): Iterator[Tile] = {
    if (to.x == from.x) {
      if (to.y == from.y) return Iterator(to.tile)
      return (from.y / 32 to to.y / 32 by Maff.signum(to.y - from.y)).iterator.map(tileY => Tile(to.x / 32, tileY))
    } else  if (to.y == from.y) {
      return (from.x / 32 to to.x / 32 by Maff.signum(to.x - from.x)).iterator.map(tileX => Tile(tileX, to.y / 32))
    }

    return Generator(from, to)
  }
}
