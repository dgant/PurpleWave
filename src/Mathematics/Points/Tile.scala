package Mathematics.Points

import Information.Geography.Types.{Base, Zone}
import Lifecycle.With
import Mathematics.PurpleMath
import bwapi.TilePosition

case class Tile(argX: Int, argY: Int) extends AbstractPoint(argX, argY) {
  
  def this(i: Int) = this(i % With.mapTileWidth, i / With.mapTileWidth )
  def this(tilePosition: TilePosition) = this(tilePosition.getX, tilePosition.getY)
  
  def bwapi: TilePosition = new TilePosition(x, y)
  
  // Performance optimization: This is not a strict equality check!
  // If you try to compare a Tile to a non-Tile you can get incorrect results.
  // Invalid tiles can also produce incorrect results.
  // For example, on a 256 x 256 map: (0, 1) == (-256, 0)
  override def equals(other: scala.Any): Boolean = hashCode == other.hashCode
  override def hashCode: Int = i
  
  def valid: Boolean = {
    x >= 0 &&
    y >= 0 &&
    x < With.mapTileWidth &&
    y < With.mapTileHeight
  }
  def clip: Tile = Tile(
    PurpleMath.clamp(x, 0, With.mapTileWidth - 1),
    PurpleMath.clamp(y, 0, With.mapTileHeight - 1))

  def tileDistanceFromEdge: Int = {
    var min = x
    if (y < min) min = y
    if (With.mapTileWidth  - x < min) min = With.mapTileWidth - x
    if (With.mapTileHeight - y < min) min = With.mapTileHeight - y
    min
  }
  def i: Int = {
    x + With.mapTileWidth * y
  }
  def add(dx: Int, dy: Int): Tile = {
    Tile(x + dx, y + dy)
  }
  def add(point: Point): Tile = {
    add(point.x, point.y)
  }
  def add(tile: Tile): Tile = {
    add(tile.x, tile.y)
  }
  def subtract(dx: Int, dy: Int): Tile = {
    add(-dx, -dy)
  }
  def subtract(tile: Tile): Tile = {
    subtract(tile.x, tile.y)
  }
  def multiply(scale: Int): Tile = {
    Tile(scale * x, scale * y)
  }
  def divide(scale: Int): Tile = {
    Tile(x/scale, y/scale)
  }
  def midpoint(pixel: Tile): Tile = {
    add(pixel).divide(2)
  }
  def tileDistanceManhattan(tile: Tile): Int = {
    Math.abs(x-tile.x) + Math.abs(y-tile.y)
  }
  def tileDistanceSlow(tile: Tile): Double = {
    Math.sqrt(tileDistanceSquared(tile))
  }
  def tileDistanceFast(tile: Tile): Double = {
    // Octagonal distance
    // https://en.wikibooks.org/wiki/Algorithms/Distance_approximations#Octagonal
    val dx = Math.abs(x - tile.x)
    val dy = Math.abs(y - tile.y)
    0.941256 * Math.max(dx, dy) + Math.min(dx, dy) * 0.414213562
  }
  def tileDistanceSquared(tile: Tile): Int = {
    val dx = x - tile.x
    val dy = y - tile.y
    dx * dx + dy * dy
  }
  def topLeftPixel: Pixel = {
    Pixel(x * 32, y * 32)
  }
  def bottomRightPixel: Pixel = {
    Pixel(x * 32 + 31, y * 32 + 31)
  }
  def pixelCenter: Pixel = {
    Pixel(x * 32 + 15, y * 32 + 15)
  }
  def topLeftWalkPixel: WalkTile = {
    WalkTile(x*4, y*4)
  }
  def left: Tile = {
    Tile(x-1, y)
  }
  def right: Tile = {
    Tile(x+1, y)
  }
  def up: Tile = {
    Tile(x, y-1) //Remember our flipped coordinate system
  }
  def down: Tile = {
    Tile(x, y+1) //Remember our flipped coordinate system
  }
  def adjacent4: Array[Tile] = {
    Array(up, down, left, right)
  }
  def adjacent5: Array[Tile] = {
    Array(this, up, down, left, right)
  }
  def adjacent8: Array[Tile] = {
    Array(up, down, left, right, up.left, up.right, down.left, down.right)
  }
  def adjacent9: Array[Tile] = {
    Array(this, up, down, left, right, up.left, up.right, down.left, down.right)
  }
  def zone: Zone = {
    With.geography.zoneByTile(this)
  }
  def base: Option[Base] = {
    With.geography.baseByTile(this)
  }
  def groundPixels(other: Pixel): Double = {
    With.paths.groundPixels(pixelCenter, other)
  }
  def groundPixels(other: Tile): Double = {
    With.paths.groundPixels(pixelCenter, other.pixelCenter)
  }
  def altitudeBonus: Double = {
    With.grids.altitudeBonus.get(this)
  }
  def toRectangle: TileRectangle = {
    TileRectangle(this, this.add(1, 1))
  }
  
}
