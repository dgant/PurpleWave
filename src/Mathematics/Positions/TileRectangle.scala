package Mathematics.Positions

import Utilities.EnrichPosition._
import bwapi.{Position, TilePosition}

case class TileRectangle(
 val startInclusive : TilePosition,
 val endExclusive   : TilePosition) {
  
  if (endExclusive.getX < startInclusive.getX || endExclusive.getY < startInclusive.getY) {
    throw new Exception("Created an invalid (non-normalized) rectangle")
  }
  
  def add(x:Int, y:Int):TileRectangle =
    new TileRectangle(
      startInclusive.add(x, y),
      endExclusive.add(x, y))
  
  def expand(x:Int, y:Int):TileRectangle =
    new TileRectangle(
      startInclusive.add(-x, -y),
      endExclusive  .add( x,  y))
  
  def add(tilePosition:TilePosition):TileRectangle =
    add(tilePosition.getX, tilePosition.getY)

  lazy val midPixel : Position     = startPixel.midpoint(endPixel)
  lazy val midpoint : TilePosition = startInclusive.midpoint(endExclusive)
  
  def contains(x:Int, y:Int):Boolean =
    x >= startInclusive.getX &&
    y >= startInclusive.getY &&
    x < endExclusive.getX &&
    y < endExclusive.getY
  
  def contains(point:TilePosition):Boolean = {
    contains(point.getX, point.getY)
  }
  
  def intersects(otherRectangle: TileRectangle):Boolean = {
    containsRectangle(otherRectangle) || otherRectangle.containsRectangle(this)
  }
  
  private def containsRectangle(otherRectangle:TileRectangle):Boolean = {
    contains(otherRectangle.startInclusive)                                             ||
    contains(otherRectangle.endExclusive.subtract(1, 1))                                ||
    contains(otherRectangle.startInclusive.getX, otherRectangle.endExclusive.getY - 1)  ||
    contains(otherRectangle.endExclusive.getX - 1, otherRectangle.startInclusive.getY)
  }
  
  lazy val startPixel : Position = startInclusive.topLeftPixel
  lazy val endPixel   : Position = endExclusive.topLeftPixel.subtract(1, 1)
  
  lazy val tiles: Array[TilePosition] = {
    // Scala while-loops are way faster than for-loops because they don't create Range objects
    val startX = startInclusive.getX
    val startY = startInclusive.getY
    val sizeX = endExclusive.getX - startX
    val sizeY = endExclusive.getY - startY
    val output = new Array[TilePosition](sizeX * sizeY)
    var x = 0
    while (x < sizeX) {
      var y = 0
      while (y < sizeY) {
        output(x + sizeX * y) = new TilePosition(startX + x, startY + y)
        y += 1
      }
      x += 1
    }
    output
  }
}
