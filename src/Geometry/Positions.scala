package Geometry

import bwapi.{Position, TilePosition, WalkPosition}

object Positions {
  def toWalkPosition(tilePosition: TilePosition) =
      new WalkPosition(
        tilePosition.getX * 8,
        tilePosition.getY * 8)
  
  def toWalkRectangle(tileRectangle: TileRectangle):WalkRectangle = {
    new WalkRectangle(
      toWalkPosition(tileRectangle.start),
      toWalkPosition(tileRectangle.end))
  }
  
  def centroid(positions:Iterable[Position]):Position = {
    if (positions.isEmpty) new Position(0, 0)
    new Position(
      positions.map(_.getX).sum / positions.size,
      positions.map(_.getY).sum / positions.size)
  }
  
  def centroid(positions:Iterable[TilePosition]):TilePosition = {
    if (positions.isEmpty) new TilePosition(0, 0)
    new TilePosition(
      positions.map(_.getX).sum / positions.size,
      positions.map(_.getY).sum / positions.size)
  }
}
