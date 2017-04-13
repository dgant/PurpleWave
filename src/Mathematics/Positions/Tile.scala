package Mathematics.Pixels

import Information.Geography.Types.Zone
import Lifecycle.With
import bwapi.{TilePosition, WalkPosition}

case class Tile(val x:Int, val y:Int) {
  
  def this(tilePosition:TilePosition) = this(tilePosition.getX, tilePosition.getY)
  
  def bwapi:TilePosition = new TilePosition(x, y)
  
  //Checking position validity is a frequent operation,
  //but going through BWAPI via BWMirror has a lot of overhead
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
  def add (point:Point):Tile = {
    add(point.x, point.y)
  }
  def add(otherTile:Tile):Tile = {
    add(otherTile.x, otherTile.y)
  }
  def subtract(dx:Int, dy:Int):Tile = {
    add(-dx, -dy)
  }
  def subtract(otherTile:Tile):Tile = {
    subtract(otherTile.x, otherTile.y)
  }
  def multiply(scale:Int):Tile = {
    new Tile(scale * x, scale * y)
  }
  def divide(scale:Int):Tile = {
    new Tile(x / scale, y / scale)
  }
  def midpoint(otherPixel:Tile):Tile = {
    add(otherPixel).divide(2)
  }
  def tileDistance(otherTile:Tile):Double = {
    Math.sqrt(distanceTileSquared(otherTile))
  }
  def distanceTileSquared(otherTile:Tile):Int = {
    val dx = x - otherTile.x
    val dy = y - otherTile.y
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
  def topLeftWalkPixel:WalkPosition = {
    new WalkPosition(x/4, y/4)
  }
  def zone:Zone = {
    With.geography.zoneByTile(this)
  }
}
