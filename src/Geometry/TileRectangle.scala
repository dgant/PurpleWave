package Geometry

import bwapi.{Position, TilePosition}
import Utilities.Enrichment.EnrichPosition._

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
  
  def add(tilePosition:TilePosition):TileRectangle =
    add(tilePosition.getX, tilePosition.getY)
  
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
    _intersects(otherRectangle) || otherRectangle._intersects(this)
  }
  
  def _intersects(otherRectangle:TileRectangle):Boolean = {
    contains(otherRectangle.startInclusive) ||
    contains(otherRectangle.endExclusive.subtract(1, 1)) ||
    contains(otherRectangle.startInclusive.getX, otherRectangle.endExclusive.getY - 1) ||
    contains(otherRectangle.endExclusive.getX - 1, otherRectangle.startInclusive.getY)
  }
  
  def startPosition:Position = {
    startInclusive.toPosition
  }
  
  def endPosition:Position = {
    endExclusive.toPosition.subtract(1, 1)
  }
  
  def toWalkRectangle:WalkRectangle = {
    new WalkRectangle(
      startInclusive.toWalkPosition,
      endExclusive.toWalkPosition)
  }
  
  def tiles:Iterable[TilePosition] = {
    (startInclusive.getX until endExclusive.getX).flatten(x =>
      (startInclusive.getY until endExclusive.getY).map(y =>
        new TilePosition(x, y)
      ))
  }
}
