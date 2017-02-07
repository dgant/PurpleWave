package Geometry

import bwapi.TilePosition

class TileRectangle(
  val start:TilePosition,
  val end:TilePosition) {
  
  if (end.getX < start.getX || end.getY < start.getY) {
    throw new Exception("Created an invalid (non-normalized) rectangle")
  }
  
  def contains(x:Int, y:Int):Boolean = {
    x >= start.getX &&
    y >= start.getY &&
    x <= end.getX &&
    y <= end.getY
  }
  
  def contains(point:TilePosition):Boolean = {
    contains(point.getX, point.getY)
  }
  
  def intersects(rectangle: TileRectangle):Boolean = {
    contains(rectangle.start) ||
    contains(rectangle.end) ||
    contains(rectangle.start.getX, rectangle.end.getY) ||
    contains(rectangle.end.getX, rectangle.start.getY)
  }
}
