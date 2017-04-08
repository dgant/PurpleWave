package Mathematics.Positions

import Utilities.EnrichPosition._
import bwapi.{Position, TilePosition}

case class TileRectangle(
 val startInclusive:TilePosition,
 val endExclusive:TilePosition) {
  
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
    contains(otherRectangle.startInclusive) ||
    contains(otherRectangle.endExclusive.subtract(1, 1)) ||
    contains(otherRectangle.startInclusive.getX, otherRectangle.endExclusive.getY - 1) ||
    contains(otherRectangle.endExclusive.getX - 1, otherRectangle.startInclusive.getY)
  }
  
  lazy val startPixel : Position = startInclusive.toPosition
  lazy val endPixel   : Position = endExclusive.toPosition.subtract(1, 1)
  
  lazy val tiles:Iterable[TilePosition] =
    for (x <- startInclusive.getX until endExclusive.getX; y <- startInclusive.getY until endExclusive.getY)
      yield new TilePosition(x, y)
}
