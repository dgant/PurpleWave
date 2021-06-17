package Mathematics.Points

import Information.Geography.Types.{Base, Zone}
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Shapes.Spiral
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.Position

final case class Pixel(argX: Int, argY: Int) extends AbstractPoint(argX, argY) {
  
  def this(position: Position) = this(position.getX, position.getY)
  
  @inline def bwapi: Position = new Position(x, y)
  @inline def valid: Boolean = {
    x >= 0 &&
    y >= 0 &&
    x < With.mapPixelWidth &&
    y < With.mapPixelHeight
  }
  @inline def pixelDistanceFromEdge: Int = {
    var min = x
    if (y < min) min = y
    if (With.mapPixelWidth  - x < min) min = With.mapPixelWidth - x
    if (With.mapPixelHeight - y < min) min = With.mapPixelHeight - y
    min
  }
  @inline def +(other: Pixel): Pixel = add(other)
  @inline def -(other: Pixel): Pixel = subtract(other)
  @inline def /(value: Int): Pixel = if (value == 0) this else Pixel(x / value, y / value)
  @inline def *(value: Int): Pixel = Pixel(x * value, y * value)
  @inline def /(value: Double): Pixel = if (value == 0) this else Pixel((x / value).toInt, (y / value).toInt)
  @inline def *(value: Double): Pixel = Pixel((x * value).toInt, (y * value).toInt)
  @inline def add(dx: Int, dy: Int): Pixel = Pixel(x + dx, y + dy)
  @inline def add(point: Point): Pixel = add(point.x, point.y)
  @inline def add(pixel: Pixel): Pixel = add(pixel.x, pixel.y)
  @inline def subtract(dx: Int, dy: Int): Pixel = add(-dx, -dy)
  @inline def subtract(otherPixel: Pixel): Pixel = subtract(otherPixel.x, otherPixel.y)
  @inline def multiply(scale: Int): Pixel = Pixel(scale * x, scale * y)
  @inline def multiply(scale: Double): Pixel = Pixel((scale * x).toInt, (scale * y).toInt)
  @inline def divide(scale: Int): Pixel = Pixel(x / scale, y / scale)
  @inline def clamp(margin: Int = 0): Pixel = {
    Pixel(
      Maff.clamp(x, margin, With.mapPixelWidth - margin),
      Maff.clamp(y, margin, With.mapPixelHeight - margin))
  }
  @inline def project(destination: Pixel, pixels: Double): Pixel = {
    if (pixels == 0) return this
    val distance = pixelDistance(destination)
    if (distance == 0) return this
    val delta = destination.subtract(this)
    delta.multiply(pixels/distance).add(this)
  }
  @inline def projectUpTo(destination: Pixel, maxPixels: Double): Pixel = {
    if (maxPixels * maxPixels >= pixelDistanceSquared(destination)) destination else project(destination, maxPixels)
  }
  @inline def radiateRadians(angleRadians: Double, pixels: Double): Pixel = {
    add(
      (pixels * Math.cos(angleRadians)).toInt,
      (pixels * Math.sin(angleRadians)).toInt)
  }
  @inline def degreesTo(other: Pixel): Double = {
    radiansTo(other) / radiansOverDegrees
  }
  @inline def radiansTo(other: Pixel): Double = {
    Maff.fastAtan2(other.y - y, other.x - x)
  }
  @inline def midpoint(pixel: Pixel): Pixel = {
    add(pixel).divide(2)
  }
  @inline def pixelDistance(pixel: Pixel): Double = {
    Maff.broodWarDistance(x, y, pixel.x, pixel.y)
  }
  @inline def pixelDistanceSquared(other: Pixel): Int = {
    val dx = x - other.x
    val dy = y - other.y
    dx * dx + dy * dy
  }
  @inline def tile: Tile = {
    Tile(x / 32, y / 32) // Note! This will handle negative coordinates incorrectly
  }
  @inline def toPoint: Point = {
    Point(x, y)
  }
  @inline def zone: Zone = {
    tile.zone
  }
  @inline def base: Option[Base] = {
    tile.base
  }
  @inline def groundPixels(other: Tile): Double = {
    With.paths.groundPixels(this, other.center)
  }
  @inline def groundPixels(other: Pixel): Double = {
    With.paths.groundPixels(this, other)
  }
  @inline def travelPixelsFor(other: Pixel, unit: UnitInfo): Double = {
    unit.pixelDistanceTravelling(this, other)
  }
  @inline def travelPixelsFor(other: Tile, unit: UnitInfo): Double = {
    unit.pixelDistanceTravelling(tile, other)
  }
  @inline def buildable: Boolean = {
    tile.buildable
  }
  @inline def buildableUnchecked: Boolean = {
    tile.buildableUnchecked
  }
  @inline def visible: Boolean = {
    tile.visible
  }
  @inline def walkable: Boolean = {
    tile.walkable
  }
  @inline def walkableUnchecked: Boolean = {
    tile.walkableUnchecked
  }
  @inline def altitude: Double = {
    tile.altitude
  }
  @inline def altitudeUnchecked: Double = {
    tile.altitudeUnchecked
  }

  @inline private def nwtTest(tile: Tile): Option[Tile] = if (tile.walkable) Some(tile) else None
  @inline private def nwtFlip(xFirst: Boolean, t0: Tile, t1: Tile): Option[Tile] = if (xFirst)
    nwtTest(t0).orElse(nwtTest(t1))
  else
    nwtTest(t1).orElse(nwtTest(t0))
  @inline def nearestWalkableTile: Tile = {
    val ti = tile
    if (ti.walkable) return ti
    val tx = x / 32
    val ty = y / 32
    val dx = if (x % 32 < 16) -1 else 1
    val dy = if (y % 32 < 16) -1 else 1
    val xFirst = Math.abs(16 - (x % 32)) > Math.abs(16 - (y % 32))
    val output =
              nwtFlip(xFirst, Tile(tx + dx, ty), Tile(tx, ty + dy))
      .orElse(nwtTest(        Tile(tx + dx, ty + dy)))
      .orElse(nwtFlip(xFirst, Tile(tx + dx, ty - dy), Tile(tx - dx, ty + dy)))
      .orElse(nwtTest(        Tile(tx - dx, ty - dy)))
      .orElse(Spiral.points(16).view.map(ti.add).find(_.walkable))
      .getOrElse(tile)
    output
  }
  @inline def traversableBy(unit: UnitInfo): Boolean = {
    unit.flying || walkable
  }
  @inline def nearestTraversableBy(unit: UnitInfo): Pixel = {
    if (unit.flying) this else nearestWalkablePixel
  }
  @inline def nearestWalkablePixel: Pixel = if (walkable) this else {
    val center = nearestWalkableTile.center
    Pixel(
      Maff.clamp(x, center.x - 16, center.x + 16),
      Maff.clamp(y, center.y - 16, center.y + 16))
  }
  @inline def nearestTraversablePixel(unit: UnitInfo): Pixel = if (unit.flying) this else {
    val center = nearestWalkableTile.center
    Pixel(
      Maff.clamp(x, center.x - 16 + Math.min(16, unit.unitClass.dimensionLeft), center.x + 16 - Math.min(16, unit.unitClass.dimensionRight)),
      Maff.clamp(y, center.y - 16 + Math.min(16, unit.unitClass.dimensionUp),   center.y + 16 - Math.min(16, unit.unitClass.dimensionDown)))
  }
  @inline def offsetFromTileCenter: Pixel = Pixel(x % 32 - 16, y % 32 - 16)

  override def toString: String = f"[$x, $y](${Maff.signum(x) * Math.abs(x/32)}, ${Maff.signum(y) * Math.abs(y/32)})"
}
