package Mathematics.Pixels

import Information.Geography.Types.Zone
import Lifecycle.With
import bwapi.Position

case class Pixel(val x:Int, val y:Int) {
  
  def this(position:Position) = this(position.getX, position.getY)
  
  def bwapi:Position = new Position(x, y)
  
  def valid:Boolean = {
    x >= 0 &&
      y >= 0 &&
      x < With.mapWidth * 32 &&
      y < With.mapHeight * 32
  }
  def add(dx:Int, dy:Int):Pixel = {
    new Pixel(x + dx, y + dy)
  }
  def add (point:Point):Pixel = {
    add(point.x, point.y)
  }
  def add(otherPixel:Pixel):Pixel = {
    add(otherPixel.x, otherPixel.y)
  }
  def subtract(dx:Int, dy:Int):Pixel = {
    add(-dx, -dy)
  }
  def subtract(otherPixel:Pixel):Pixel = {
    subtract(otherPixel.x, otherPixel.y)
  }
  def multiply(scale:Int):Pixel = {
    new Pixel(scale * x, scale * y)
  }
  def multiply(scale:Double):Pixel = {
    new Pixel((scale * x).toInt, (scale * y).toInt)
  }
  def divide(scale:Int):Pixel = {
    new Pixel(x / scale, y / scale)
  }
  def project(destination:Pixel, pixels:Double):Pixel = {
    if (pixels == 0) return this
    val distance = pixelDistanceSlow(destination)
    if (distance == 0) return this
    val delta = destination.subtract(this)
    delta.multiply(pixels/distance).add(this)
  }
  def midpoint(otherPixel:Pixel):Pixel = {
    add(otherPixel).divide(2)
  }
  def pixelDistanceSlow(otherPixel:Pixel):Double = {
    Math.sqrt(pixelDistanceSquared(otherPixel))
  }
  def pixelDistanceFast(otherPixel:Pixel):Double = {
    // Octagonal distance
    // https://en.wikibooks.org/wiki/Algorithms/Distance_approximations#Octagonal
    val dx = Math.abs(x - otherPixel.x)
    val dy = Math.abs(y - otherPixel.y)
    0.941256 * Math.max(dx, dy) + Math.min(dx, dy) * 0.414213562
  }
  def pixelDistanceSquared(otherPixel:Pixel):Int = {
    val dx = x - otherPixel.x
    val dy = y - otherPixel.y
    dx * dx + dy * dy
  }
  def tileIncluding:Tile = {
    new Tile(x/32, y/32)
  }
  def tileNearest:Tile = {
    add(16, 16).tileIncluding
  }
  def toPoint:Point = {
    new Point(x, y)
  }
  def zone:Zone = {
    With.geography.zoneByTile(tileIncluding)
  }
}
