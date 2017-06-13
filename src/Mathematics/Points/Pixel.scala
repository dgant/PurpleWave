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
    Pixel(x + dx, y + dy)
  }
  def add(point:Point):Pixel = {
    add(point.x, point.y)
  }
  def add(pixel:Pixel):Pixel = {
    add(pixel.x, pixel.y)
  }
  def subtract(dx:Int, dy:Int):Pixel = {
    add(-dx, -dy)
  }
  def subtract(otherPixel:Pixel):Pixel = {
    subtract(otherPixel.x, otherPixel.y)
  }
  def multiply(scale:Int):Pixel = {
    Pixel(scale * x, scale * y)
  }
  def multiply(scale:Double):Pixel = {
    Pixel((scale * x).toInt, (scale * y).toInt)
  }
  def divide(scale:Int):Pixel = {
    Pixel(x / scale, y / scale)
  }
  def project(destination:Pixel, pixels:Double):Pixel = {
    if (pixels == 0) return this
    val distance = pixelDistanceSlow(destination)
    if (distance == 0) return this
    val delta = destination.subtract(this)
    delta.multiply(pixels/distance).add(this)
  }
  private val radiansOverAngle = 2.0 * Math.PI / 256.0
  def radiate(angle: Double, pixels: Double):Pixel = {
    // According to JohnJ, Brood War understands 256 angles
    // The BWAPI interface reduces this to a double (radians) with the cartesian origin (pointing right)
    // We'll use 256 (to match the engine behavior) and the BWAPI origin
    val radians = radiansOverAngle * angle
    add(
      (pixels * Math.cos(radians)).toInt,
      (pixels * Math.sin(radians)).toInt)
  }
  def degreesTo(other:Pixel):Double = {
    Math.atan2(other.y - y, other.x - x) / radiansOverAngle
  }
  def radiansTo(other:Pixel):Double = {
    Math.atan2(other.y - y, other.x - x)
  }
  def midpoint(pixel:Pixel):Pixel = {
    add(pixel).divide(2)
  }
  def pixelDistanceSlow(pixel:Pixel):Double = {
    Math.sqrt(pixelDistanceSquared(pixel))
  }
  def pixelDistanceFast(pixel:Pixel):Double = {
    // Octagonal distance
    // https://en.wikibooks.org/wiki/Algorithms/Distance_approximations#Octagonal
    val dx = Math.abs(x - pixel.x)
    val dy = Math.abs(y - pixel.y)
    0.941256 * Math.max(dx, dy) + Math.min(dx, dy) * 0.414213562
  }
  def pixelDistanceSquared(pixel:Pixel):Int = {
    val dx = x - pixel.x
    val dy = y - pixel.y
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
