package Geometry

import bwapi.{TilePosition, WalkPosition}

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
}
