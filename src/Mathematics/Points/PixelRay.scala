package Mathematics.Points

import Mathematics.PurpleMath

case class PixelRay(from: Pixel, to: Pixel) {
  
  def this(from: Pixel, lengthPixels: Double, angleRadians: Double) {
    this(from, from.radiateRadians(angleRadians, lengthPixels))
  }
  
  def lengthFast: Double = from.pixelDistanceFast(to)
  def lengthSlow: Double = from.pixelDistanceSlow(to)
  
  def tilesIntersected: Array[Tile] = {
    
    if (to.x == from.x) return (from.y / 32 to to.y / 32).map(tileY => Tile(to.x/32, tileY)).toArray
    if (to.y == from.y) return (from.x / 32 to to.x / 32).map(tileX => Tile(tileX, to.y/32)).toArray
    
    val gridSize    = 32
    val signX       = PurpleMath.signum(to.x - from.x)
    val signY       = PurpleMath.signum(to.y - from.y)
    var x           = from.x
    var y           = from.y
    val velocityX   = to.x - from.x.toDouble
    val velocityY   = to.y - from.y.toDouble
    val tileCount   = 1 + from.tileIncluding.tileDistanceManhattan(to.tileIncluding)
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
