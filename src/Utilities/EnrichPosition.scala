package Utilities

import Mathematics.Positions
import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Positions.{Point, TileRectangle}
import bwapi.{Position, TilePosition, WalkPosition}

case object EnrichPosition {
  implicit class EnrichedPositionCollection(positions:Iterable[Position]) {
    def minBound:Position = {
      if (positions.isEmpty) return Positions.Positions.middle
      new Position(
        positions.view.map(_.getX).min,
        positions.view.map(_.getY).min)}
    def maxBound:Position = {
      if (positions.isEmpty) return Positions.Positions.middle
      new Position(
        positions.view.map(_.getX).max,
        positions.view.map(_.getY).max)}
    def bottomLeftBound:Position = {
      if (positions.isEmpty) return Positions.Positions.middle
      new Position(
        positions.view.map(_.getX).min,
        positions.view.map(_.getY).max)}
    def topRightBound:Position = {
      if (positions.isEmpty) return Positions.Positions.middle
      new Position(
        positions.view.map(_.getX).max,
        positions.view.map(_.getY).min)}
    def centroid:Position = {
      if (positions.isEmpty) return Positions.Positions.middle
      new Position(
        positions.view.map(_.getX).sum / positions.size,
        positions.view.map(_.getY).sum / positions.size)
    }
  }
  
  implicit class EnrichedRectangleCollection(rectangles:Iterable[TileRectangle]) {
    def boundary: TileRectangle =
      new TileRectangle(
        new TilePosition(
          rectangles.map(_.startInclusive.getX).min,
          rectangles.map(_.startInclusive.getY).min),
        new TilePosition(
          rectangles.map(_.endExclusive.getX).max,
          rectangles.map(_.endExclusive.getY).max))
  }
  
  implicit class EnrichedTilePositionCollection(positions:Iterable[TilePosition]) {
    def minBound:TilePosition = {
      if (positions.isEmpty) return Positions.Positions.tileMiddle
      new TilePosition(
        positions.view.map(_.getX).min,
        positions.view.map(_.getY).min)}
    def maxBound:TilePosition = {
      if (positions.isEmpty) return Positions.Positions.tileMiddle
      new TilePosition(
        positions.view.map(_.getX).max,
        positions.view.map(_.getY).max)}
    def centroid:TilePosition = {
      if (positions.isEmpty) return Positions.Positions.tileMiddle
      new TilePosition(
        positions.view.map(_.getX).sum / positions.size,
        positions.view.map(_.getY).sum / positions.size)
    }
  }
  
  implicit class EnrichedPosition(pixel:Position) {
    //Checking position validity is a frequent operation,
    //but going through BWAPI via BWMirror has a lot of overhead
    def valid:Boolean = {
      pixel.getX >= 0 &&
      pixel.getY >= 0 &&
      pixel.getX < With.mapWidth * 32 &&
      pixel.getY < With.mapHeight * 32
    }
    def add(dx:Int, dy:Int):Position = {
      new Position(pixel.getX + dx, pixel.getY + dy)
    }
    def add (point:Point):Position = {
      add(point.x, point.y)
    }
    def add(otherPosition:Position):Position = {
      add(otherPosition.getX, otherPosition.getY)
    }
    def subtract(dx:Int, dy:Int):Position = {
      add(-dx, -dy)
    }
    def subtract(otherPosition:Position):Position = {
      subtract(otherPosition.getX, otherPosition.getY)
    }
    def multiply(scale:Int):Position = {
      new Position(scale * pixel.getX, scale * pixel.getY)
    }
    def multiply(scale:Double):Position = {
      new Position((scale * pixel.getX).toInt, (scale * pixel.getY).toInt)
    }
    def divide(scale:Int):Position = {
      new Position(pixel.getX / scale, pixel.getY / scale)
    }
    def project(destination:Position, pixels:Double):Position = {
      if (pixels == 0) return pixel
      val distance = pixelDistanceSlow(destination)
      if (distance == 0) return pixel
      val delta = destination.subtract(pixel)
      delta.multiply(pixels/distance).add(pixel)
    }
    def midpoint(otherPixel:Position):Position = {
      add(otherPixel).divide(2)
    }
    def pixelDistanceSlow(otherPixel:Position):Double = {
      Math.sqrt(pixelDistanceSquared(otherPixel))
    }
    def pixelDistanceSquared(otherPosition:Position):Int = {
      val dx = pixel.getX - otherPosition.getX
      val dy = pixel.getY - otherPosition.getY
      dx * dx + dy * dy
    }
    def tileIncluding:TilePosition = {
      pixel.toTilePosition
    }
    def tileNearest:TilePosition = {
      pixel.add(16, 16).toTilePosition
    }
    def toPoint:Point = {
      new Point(pixel.getX, pixel.getY)
    }
    def toWalkPosition:WalkPosition = {
      new WalkPosition(pixel.getX / 4, pixel.getY / 4)
    }
    def zone:Zone = {
      With.geography.zoneByTile(tileIncluding)
    }
  }
  
  implicit class EnrichedTilePosition(tile:TilePosition) {
    //Checking position validity is a frequent operation,
    //but going through BWAPI via BWMirror has a lot of overhead
    def valid:Boolean = {
      tile.getX >= 0 &&
      tile.getY >= 0 &&
      tile.getX < With.mapWidth &&
      tile.getY < With.mapHeight
    }
    def add(dx:Int, dy:Int):TilePosition = {
      new TilePosition(tile.getX + dx, tile.getY + dy)
    }
    def add (point:Point):TilePosition = {
      add(point.x, point.y)
    }
    def add(otherTile:TilePosition):TilePosition = {
      add(otherTile.getX, otherTile.getY)
    }
    def subtract(dx:Int, dy:Int):TilePosition = {
      add(-dx, -dy)
    }
    def subtract(otherTile:TilePosition):TilePosition = {
      subtract(otherTile.getX, otherTile.getY)
    }
    def multiply(scale:Int):TilePosition = {
      new TilePosition(scale * tile.getX, scale * tile.getY)
    }
    def divide(scale:Int):TilePosition = {
      new TilePosition(tile.getX / scale, tile.getY / scale)
    }
    def midpoint(otherPosition:TilePosition):TilePosition = {
      add(otherPosition).divide(2)
    }
    def tileDistance(otherTile:TilePosition):Double = {
      Math.sqrt(distanceTileSquared(otherTile))
    }
    def distanceTileSquared(otherTile:TilePosition):Int = {
      val dx = tile.getX - otherTile.getX
      val dy = tile.getY - otherTile.getY
      dx * dx + dy * dy
    }
    def topLeftPixel:Position = {
      tile.toPosition
    }
    def bottomRightPixel:Position = {
      tile.toPosition.add(31, 31)
    }
    def pixelCenter:Position = {
      tile.toPosition.add(16, 16)
    }
    def topLeftWalkPosition:WalkPosition = {
      new WalkPosition(tile.getX * 4, tile.getY * 4)
    }
    def zone:Zone = {
      With.geography.zoneByTile(tile)
    }
  }
  
  implicit class EnrichedWalkPosition(position:WalkPosition) {
    def add(dx:Int, dy:Int):WalkPosition = {
      new WalkPosition(position.getX + dx, position.getY + dy)
    }
    def add(point:Point):WalkPosition = {
      add(point.x, point.y)
    }
    def add(otherPosition:WalkPosition):WalkPosition = {
      add(otherPosition.getX, otherPosition.getY)
    }
    def subtract(dx:Int, dy:Int):WalkPosition = {
      add(-dx, -dy)
    }
    def subtract(otherPosition:WalkPosition):WalkPosition = {
      subtract(otherPosition.getX, otherPosition.getY)
    }
    def multiply(scale:Int):WalkPosition = {
      new WalkPosition(scale * position.getX, scale * position.getY)
    }
    def divide(scale:Int):WalkPosition = {
      new WalkPosition(position.getX / scale, position.getY / scale)
    }
    def midpoint(otherPosition:WalkPosition):WalkPosition = {
      add(otherPosition).divide(2)
    }
    def distanceWalk(otherPosition:WalkPosition):Double = {
      Math.sqrt(distanceWalkSquared(otherPosition))
    }
    def distanceWalkSquared(otherPosition:WalkPosition):Int = {
      val dx = position.getX - otherPosition.getX
      val dy = position.getY - otherPosition.getY
      dx * dx + dy * dy
    }
  }
}
