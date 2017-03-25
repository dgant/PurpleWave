package Geometry

import bwapi.{Position, TilePosition}
import Utilities.EnrichPosition._

class TileRectangle(
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

  def midPixel:Position = startPixel.midpoint(endPixel)
  def midpoint:TilePosition = startInclusive.midpoint(endExclusive)
  
  def contains(x:Int, y:Int):Boolean = {
    x >= startInclusive.getX &&
    y >= startInclusive.getY &&
    x < endExclusive.getX &&
    y < endExclusive.getY
  }
  
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
  
  def startPixel:Position = {
    startInclusive.toPosition
  }
  
  def endPixel:Position = {
    endExclusive.toPosition.subtract(1, 1)
  }
  
  def tiles:Iterable[TilePosition] = {
    (startInclusive.getX until endExclusive.getX).flatten(x =>
      (startInclusive.getY until endExclusive.getY).map(y =>
        new TilePosition(x, y)
      ))
  }
}
