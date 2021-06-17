package Mathematics.Points

import Mathematics.Maff

object PixelRay {

  @inline final def apply(from: Pixel, lengthPixels: Double, radians: Double): Iterable[Tile] = {
    apply(from, from.radiateRadians(radians, lengthPixels))
  }
  
  @inline final def apply(from: Pixel, to: Pixel): Iterable[Tile] = {
    if (to.x == from.x) {
      val direction = Maff.signum(to.y - from.y)
      if (direction == 0) return Array(to.tile)
      return (from.y / 32 to to.y / 32 by direction).view.map(tileY => Tile(to.x/32, tileY))
    }
    if (to.y == from.y) {
      val direction = Maff.signum(to.x - from.x)
      if (direction == 0) return Array(to.tile)
      return (from.x / 32 to to.x / 32 by direction).view.map(tileX => Tile(tileX, to.y/32))
    }
    
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
    while (tileIndex < tileCount) {
      output(tileIndex)   = Tile(x/32, y/32)
      tileIndex           += 1
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
          output(tileIndex) = Tile(x/32, y/32 - signY)
          tileIndex += 1
        }
      }
    }
    output
  }
}
