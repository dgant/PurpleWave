package Mathematics.Points

import Information.Geography.Types.{Base, Zone}
import Lifecycle.With
import Mathematics.PurpleMath
import Mathematics.Shapes.Spiral
import ProxyBwapi.UnitInfo.UnitInfo
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
  @inline final def /(value: Int): Pixel = if (value == 0) this else Pixel(x / value, y / value)
  @inline final def *(value: Int): Pixel = Pixel(x * value, y * value)
  @inline final def /(value: Double): Pixel = if (value == 0) this else Pixel((x / value).toInt, (y / value).toInt)
  @inline final def *(value: Double): Pixel = Pixel((x * value).toInt, (y * value).toInt)
  @inline final def add(dx: Int, dy: Int): Pixel = Pixel(x + dx, y + dy)
  @inline final def add(point: Point): Pixel = add(point.x, point.y)
  @inline final def add(pixel: Pixel): Pixel = add(pixel.x, pixel.y)
  @inline final def subtract(dx: Int, dy: Int): Pixel = add(-dx, -dy)
  @inline final def subtract(otherPixel: Pixel): Pixel = subtract(otherPixel.x, otherPixel.y)
  @inline final def multiply(scale: Int): Pixel = Pixel(scale * x, scale * y)
  @inline final def multiply(scale: Double): Pixel = Pixel((scale * x).toInt, (scale * y).toInt)
  @inline final def divide(scale: Int): Pixel = Pixel(x / scale, y / scale)
  @inline final def clamp(margin: Int = 0): Pixel = {
    Pixel(
      PurpleMath.clamp(x, margin, With.mapPixelWidth - margin),
      PurpleMath.clamp(y, margin, With.mapPixelHeight - margin))
  }
  @inline final def project(destination: Pixel, pixels: Double): Pixel = {
    if (pixels == 0) return this
    val distance = pixelDistance(destination)
    if (distance == 0) return this
    val delta = destination.subtract(this)
    delta.multiply(pixels/distance).add(this)
  }
  @inline final def projectUpTo(destination: Pixel, maxPixels: Double): Pixel = {
    if (maxPixels * maxPixels >= pixelDistanceSquared(destination)) destination else project(destination, maxPixels)
  }
  @inline final def radiateRadians(angleRadians: Double, pixels: Double): Pixel = {
    add(
      (pixels * Math.cos(angleRadians)).toInt,
      (pixels * Math.sin(angleRadians)).toInt)
  }
  @inline final def degreesTo(other: Pixel): Double = {
    radiansTo(other) / radiansOverDegrees
  }
  @inline final def radiansTo(other: Pixel): Double = {
    PurpleMath.fastAtan2(other.y - y, other.x - x)
  }
  @inline final def midpoint(pixel: Pixel): Pixel = {
    add(pixel).divide(2)
  }
  @inline final def pixelDistance(pixel: Pixel): Double = {
    PurpleMath.broodWarDistance(x, y, pixel.x, pixel.y)
  }
  @inline final def pixelDistanceSquared(other: Pixel): Int = {
    val dx = x - other.x
    val dy = y - other.y
    dx * dx + dy * dy
  }
  @inline final def tile: Tile = {
    Tile(x / 32, y / 32) // Note! This will handle negative coordinates incorrectly
  }
  @inline final def toPoint: Point = {
    Point(x, y)
  }
  @inline final def zone: Zone = {
    tile.zone
  }
  @inline final def base: Option[Base] = {
    tile.base
  }
  @inline final def groundPixels(other: Tile): Double = {
    With.paths.groundPixels(this, other.pixelCenter)
  }
  @inline final def groundPixels(other: Pixel): Double = {
    With.paths.groundPixels(this, other)
  }
  @inline final def buildable: Boolean = {
    tile.buildable
  }
  @inline final def buildableUnchecked: Boolean = {
    tile.buildableUnchecked
  }
  @inline final def walkable: Boolean = {
    tile.walkable
  }
  @inline final def walkableUnchecked: Boolean = {
    tile.walkableUnchecked
  }
  @inline final def altitude: Double = {
    tile.altitude
  }
  @inline final def altitudeUnchecked: Double = {
    tile.altitudeUnchecked
  }
  @inline final def nearestWalkableTile: Tile = {
    val ti = tile
    if (ti.walkable) return ti
    val tx = x / 32
    val ty = y / 32
    val dx = if (x % 32 < 16) -1 else 1
    val dy = if (y % 32 < 16) -1 else 1
    val xFirst = Math.abs(16 - (x % 32)) > Math.abs(16 - (y % 32))
    def test(tile: Tile): Option[Tile] = if (tile.walkable) Some(tile) else None
    def flip(t0: Tile, t1: Tile): Option[Tile] = if (xFirst) test(t0).orElse(test(t1)) else test(t1).orElse(test(t0))
    val output =
              flip(Tile(tx + dx, ty), Tile(tx, ty + dy))
      .orElse(test(Tile(tx + dx, ty + dy)))
      .orElse(flip(Tile(tx + dx, ty - dy), Tile(tx - dx, ty + dy)))
      .orElse(test(Tile(tx - dx, ty - dy)))
      .orElse(Spiral.points(16).view.map(ti.add).find(_.walkable))
      .getOrElse(tile)
    output
  }
  @inline final def traversableBy(unit: UnitInfo): Boolean = {
    unit.flying || walkable
  }
  @inline final def nearestTraversableBy(unit: UnitInfo): Pixel = {
    if (unit.flying) this else nearestWalkablePixel
  }
  @inline final def nearestWalkablePixel: Pixel = if (walkable) this else {
    val center = nearestWalkableTile.pixelCenter
    Pixel(
      PurpleMath.clamp(x, center.x - 16, center.x + 16),
      PurpleMath.clamp(y, center.y - 16, center.y + 16))
  }
  @inline final def nearestTraversablePixel(unit: UnitInfo): Pixel = if (unit.flying) this else {
    val center = nearestWalkableTile.pixelCenter
    Pixel(
      PurpleMath.clamp(x, center.x - 16 + Math.min(16, unit.unitClass.dimensionLeft), center.x + 16 - Math.min(16, unit.unitClass.dimensionRight)),
      PurpleMath.clamp(y, center.y - 16 + Math.min(16, unit.unitClass.dimensionUp),   center.y + 16 - Math.min(16, unit.unitClass.dimensionDown)))
  }
  @inline final def offsetFromTileCenter: Pixel = Pixel(x % 32 - 16, y % 32 - 16)
}
