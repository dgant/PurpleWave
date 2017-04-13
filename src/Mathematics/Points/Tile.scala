package Mathematics.Pixels

import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Points.{AbstractPoint, WalkTile}
import bwapi.TilePosition

case class Tile(argX:Int, argY:Int) extends AbstractPoint(argX, argY) {
  
  def this(tilePosition:TilePosition) = this(tilePosition.getX, tilePosition.getY)
  def bwapi:TilePosition = new TilePosition(x, y)
  def valid:Boolean = {
    x >= 0 &&
    y >= 0 &&
    x < With.mapWidth &&
    y < With.mapHeight
  }
  def i:Int = {
    x + With.mapWidth * y
  }
  def add(dx:Int, dy:Int):Tile = {
    new Tile(x + dx, y + dy)
  }
  def add(point:Point):Tile = {
    add(point.x, point.y)
  }
  def add(tile:Tile):Tile = {
    add(tile.x, tile.y)
  }
  def subtract(dx:Int, dy:Int):Tile = {
    add(-dx, -dy)
  }
  def subtract(tile:Tile):Tile = {
    subtract(tile.x, tile.y)
  }
  def multiply(scale:Int):Tile = {
    new Tile(scale * x, scale * y)
  }
  def divide(scale:Int):Tile = {
    new Tile(x/scale, y/scale)
  }
  def midpoint(pixel:Tile):Tile = {
    add(pixel).divide(2)
  }
  def tileDistanceSlow(tile:Tile):Double = {
    Math.sqrt(tileDistanceSquared(tile))
  }
  def tileDistanceSquared(tile:Tile):Int = {
    val dx = x - tile.x
    val dy = y - tile.y
    dx * dx + dy * dy
  }
  def topLeftPixel:Pixel = {
    new Pixel(x * 32, y * 32)
  }
  def bottomRightPixel:Pixel = {
    new Pixel(x * 32 + 31, y * 32 + 31)
  }
  def pixelCenter:Pixel = {
    new Pixel(x * 32 + 15, y * 32 + 15)
  }
  def topLeftWalkPixel:WalkTile = {
    new WalkTile(x/4, y/4)
  }
  def zone:Zone = {
    With.geography.zoneByTile(this)
  }
}
