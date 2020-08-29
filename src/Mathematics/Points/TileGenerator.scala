package Mathematics.Points

import Mathematics.PurpleMath

class TileGenerator(suggestedOrigin: Tile, val boundA: Tile, val boundB: Tile, val direction: Direction) {
  private val xMin = Math.min(boundA.x, boundB.x)
  private val xMax = Math.max(boundA.x, boundB.x)
  private val yMin = Math.min(boundA.y, boundB.y)
  private val yMax = Math.max(boundA.y, boundB.y)
  private val origin = Tile(PurpleMath.clamp(suggestedOrigin.x, xMin, xMax), PurpleMath.clamp(suggestedOrigin.y, yMin, yMax))
  private val dxMax = Math.max(suggestedOrigin.x - xMin, xMax - suggestedOrigin.x)
  private val dyMax = Math.max(suggestedOrigin.y - yMin, yMax - suggestedOrigin.y)
  private val breadthIsX = direction.y != 0
  private val sideDepth = if (breadthIsX) direction.y else direction.x
  private var sideBreadth = 1
  private var dBreadth = 0
  private var dDepth = 0
  private val maxBreadth = if (breadthIsX) dxMax else dyMax
  private val maxDepth = if (breadthIsX) dyMax else dxMax
  private var nextOutput: Tile = origin
  private var _hasNext: Boolean = true

  def next(): Tile = {
    val output = nextOutput
    var foundNext: Boolean = false
    do {
      if (sideBreadth < 0) {
        dBreadth += 1
      }
      sideBreadth = -sideBreadth
      if (dBreadth > maxBreadth) {
        dBreadth = 0
        dDepth += 1
        sideBreadth = 1
      }

      _hasNext = dDepth <= maxDepth
      nextOutput = suggestedOrigin.add(
        if (breadthIsX) dBreadth * sideBreadth else dDepth * sideDepth,
        if (breadthIsX) dDepth * sideDepth else dBreadth * sideBreadth)
      foundNext = nextOutput.x >= xMin && nextOutput.x < xMax && nextOutput.y >= yMin && nextOutput.y < yMax
    } while (_hasNext && ! foundNext)
    output
  }

  def hasNext: Boolean = _hasNext
}
