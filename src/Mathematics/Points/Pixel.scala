package Mathematics.Points

import Information.Geography.Types.{Base, Zone}
import Lifecycle.With
import Mathematics.PurpleMath
import Mathematics.Shapes.Spiral
import bwapi.Position

case class Pixel(argX: Int, argY: Int) extends AbstractPoint(argX, argY) {
  
  def this(position: Position) = this(position.getX, position.getY)
  
  @inline final def bwapi: Position = new Position(x, y)
  
  @inline final def valid: Boolean = {
    x >= 0 &&
    y >= 0 &&
    x < With.mapPixelWidth &&
    y < With.mapPixelHeight
  }
  @inline final def pixelDistanceFromEdge: Int = {
    var min = x
    if (y < min) min = y
    if (With.mapPixelWidth  - x < min) min = With.mapPixelWidth - x
    if (With.mapPixelHeight - y < min) min = With.mapPixelHeight - y
    min
  }
  @inline final def +(other: Pixel): Pixel = add(other)
  @inline final def -(other: Pixel): Pixel = subtract(other)
  @inline final def add(dx: Int, dy: Int): Pixel = {
    Pixel(x + dx, y + dy)
  }
  @inline final def add(point: Point): Pixel = {
    add(point.x, point.y)
  }
  @inline final def add(pixel: Pixel): Pixel = {
    add(pixel.x, pixel.y)
  }
  @inline final def subtract(dx: Int, dy: Int): Pixel = {
    add(-dx, -dy)
  }
  @inline final def subtract(otherPixel: Pixel): Pixel = {
    subtract(otherPixel.x, otherPixel.y)
  }
  @inline final def multiply(scale: Int): Pixel = {
    Pixel(scale * x, scale * y)
  }
  @inline final def multiply(scale: Double): Pixel = {
    Pixel((scale * x).toInt, (scale * y).toInt)
  }
  @inline final def divide(scale: Int): Pixel = {
    Pixel(x / scale, y / scale)
  }
  @inline final def clamp: Pixel = {
    Pixel(
      PurpleMath.clamp(x, 0, With.mapPixelWidth),
      PurpleMath.clamp(y, 0, With.mapPixelHeight))
  }
  @inline final def project(destination: Pixel, pixels: Double): Pixel = {
    if (pixels == 0) return this
    val distance = pixelDistance(destination)
    if (distance == 0) return this
    val delta = destination.subtract(this)
    delta.multiply(pixels/distance).add(this)
  }

  @inline final def radiateRadians(angleRadians: Double, pixels: Double): Pixel = {
    add(
      (pixels * Math.cos(angleRadians)).toInt,
      (pixels * Math.sin(angleRadians)).toInt)
  }
  @inline final def radiate256Degrees(angleDegrees: Double, pixels: Double): Pixel = {
    // According to JohnJ, Brood War understands 256 angles
    // The BWAPI interface reduces this to a double (radians) with the cartesian origin (pointing right)
    // We'll use 256 (to match the engine behavior) and the BWAPI origin
    radiateRadians(radiansOverDegrees * angleDegrees, pixels)
  }
  @inline final def degreesTo(other: Pixel): Double = {
    radiansTo(other) / radiansOverDegrees
  }
  @inline final def radiansTo(other: Pixel): Double = {
    PurpleMath.atan2(other.y - y, other.x - x)
  }
  @inline final def midpoint(pixel: Pixel): Pixel = {
    add(pixel).divide(2)
  }
  @inline final def pixelDistance(pixel: Pixel): Double = PurpleMath.broodWarDistance(x, y, pixel.x, pixel.y)
  @inline final def pixelDistanceSquared(other: Pixel): Int = {
    val dx = x - other.x
    val dy = y - other.y
    dx * dx + dy * dy
  }
  @inline final def tileIncluding: Tile = {
    Tile(x/32, y/32)
  }
  @inline final def tileNearest: Tile = {
    add(16, 16).tileIncluding
  }
  @inline final def toPoint: Point = {
    Point(x, y)
  }
  @inline final def zone: Zone = {
    With.geography.zoneByTile(tileIncluding)
  }
  @inline final def base: Option[Base] = {
    With.geography.baseByTile(tileIncluding)
  }
  @inline final def groundPixels(other: Tile): Double = {
    With.paths.groundPixels(this, other.pixelCenter)
  }
  @inline final def groundPixels(other: Pixel): Double = {
    With.paths.groundPixels(this, other)
  }
  @inline final def nearestWalkableTerrain: Tile = {
    val ti = tileIncluding
    if (ti.valid && With.grids.walkable.getUnchecked(ti.i)) return ti
    val tx = x / 32
    val ty = y / 32
    val dx = if (x % 32 < 16) -1 else 1
    val dy = if (y % 32 < 16) -1 else 1
    val xFirst = Math.abs(16 - (x % 32)) > Math.abs(16 - (y % 32))
    def test(tile: Tile): Option[Tile] = if (tile.valid && With.grids.walkable.getUnchecked(tile.i)) Some(tile) else None
    def flip(t0: Tile, t1: Tile): Option[Tile] = if (xFirst) test(t0).orElse(test(t1)) else test(t1).orElse(test(t0))

    val output =
              flip(Tile(tx + dx, ty), Tile(tx, ty + dy))
      .orElse(test(Tile(tx + dx, ty + dy)))
      .orElse(flip(Tile(tx + dx, ty - dy), Tile(tx - dx, ty + dy)))
      .orElse(test(Tile(tx - dx, ty - dy)))
      .orElse(Spiral.points(16).view.map(ti.add).find(tile => tile.valid && With.grids.walkable.getUnchecked(tile.i)))
      .getOrElse(tileIncluding)
    output
  }
}
