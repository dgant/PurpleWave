package Mathematics.Points

import Information.Geography.Types.{Base, Zone}
import Lifecycle.With
import Mathematics.PurpleMath
import Mathematics.Shapes.Spiral
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.TilePosition

case class Tile(argX: Int, argY: Int) extends AbstractPoint(argX, argY) {
  
  def this(i: Int) = this(i % With.mapTileWidth, i / With.mapTileWidth )
  def this(tilePosition: TilePosition) = this(tilePosition.getX, tilePosition.getY)
  
  @inline final def bwapi: TilePosition = new TilePosition(x, y)
  
  // Performance optimization: This is not a strict equality check!
  // If you try to compare a Tile to a non-Tile you can get incorrect results.
  // Invalid tiles can also produce incorrect results.
  // For example, on a 256 x 256 map: (0, 1) == (-256, 0)
  @inline final override def equals(other: scala.Any): Boolean = hashCode == other.hashCode
  @inline final override def hashCode: Int = i
  
  @inline final def valid: Boolean = {
    x >= 0 &&
    y >= 0 &&
    x < With.mapTileWidth &&
    y < With.mapTileHeight
  }
  @inline final def clip: Tile = Tile(
    PurpleMath.clamp(x, 0, With.mapTileWidth - 1),
    PurpleMath.clamp(y, 0, With.mapTileHeight - 1))

  @inline final def tileDistanceFromEdge: Int = {
    var min = x
    if (y < min) min = y
    if (With.mapTileWidth  - x < min) min = With.mapTileWidth - x
    if (With.mapTileHeight - y < min) min = With.mapTileHeight - y
    min
  }
  val i: Int = x + With.mapTileWidth * y
  @inline final def add(dx: Int, dy: Int): Tile = {
    Tile(x + dx, y + dy)
  }
  @inline final def add(point: Point): Tile = {
    add(point.x, point.y)
  }
  @inline final def add(tile: Tile): Tile = {
    add(tile.x, tile.y)
  }
  @inline final def subtract(dx: Int, dy: Int): Tile = {
    add(-dx, -dy)
  }
  @inline final def subtract(tile: Tile): Tile = {
    subtract(tile.x, tile.y)
  }
  @inline final def multiply(scale: Int): Tile = {
    Tile(scale * x, scale * y)
  }
  @inline final def divide(scale: Int): Tile = {
    Tile(x / scale, y / scale)
  }
  @inline final def midpoint(pixel: Tile): Tile = {
    add(pixel).divide(2)
  }
  @inline final def tileDistanceManhattan(tile: Tile): Int = {
    Math.abs(x-tile.x) + Math.abs(y-tile.y)
  }
  @inline final def tileDistanceSlow(tile: Tile): Double = {
    Math.sqrt(tileDistanceSquared(tile))
  }
  @inline private val sqrt2m1 = {
    Math.sqrt(2) - 1
  }
  @inline final def tileDistanceFast(tile: Tile): Double = {
    // Octagonal distance
    // https://en.wikibooks.org/wiki/Algorithms/Distance_approximations#Octagonal
    val dx = Math.abs(x - tile.x)
    val dy = Math.abs(y - tile.y)
    0.941256 * Math.max(dx, dy) + Math.min(dx, dy) * sqrt2m1
  }
  @inline final def tileDistanceSquared(tile: Tile): Int = {
    val dx = x - tile.x
    val dy = y - tile.y
    dx * dx + dy * dy
  }
  @inline final def topLeftPixel: Pixel = {
    Pixel(x * 32, y * 32)
  }
  @inline final def topRightPixel: Pixel = {
    Pixel(x * 32 + 31, y * 32)
  }
  @inline final def bottomRightPixel: Pixel = {
    Pixel(x * 32 + 31, y * 32 + 31)
  }
  @inline final def bottomLeftPixel: Pixel = {
    Pixel(x * 32, y * 32 + 31)
  }
  @inline final def pixelCorners: Array[Pixel] = {
    Array(topLeftPixel, topRightPixel, bottomRightPixel, bottomLeftPixel)
  }
  @inline final def pixelCenter: Pixel = {
    Pixel(x * 32 + 15, y * 32 + 15)
  }
  @inline final def topLeftWalkPixel: WalkTile = {
    WalkTile(x * 4, y * 4)
  }
  @inline final def left: Tile = {
    Tile(x - 1, y)
  }
  @inline final def right: Tile = {
    Tile(x+1, y)
  }
  @inline final def up: Tile = {
    Tile(x, y-1) //Remember our flipped coordinate system
  }
  @inline final def down: Tile = {
    Tile(x, y+1) //Remember our flipped coordinate system
  }
  @inline final def adjacent4: Array[Tile] = {
    Array(up, down, left, right)
  }
  @inline final def adjacent5: Array[Tile] = {
    Array(this, up, down, left, right)
  }
  @inline final def adjacent8: Array[Tile] = {
    Array(up, down, left, right, up.left, up.right, down.left, down.right)
  }
  @inline final def adjacent9: Array[Tile] = {
    Array(this, up, down, left, right, up.left, up.right, down.left, down.right)
  }
  @inline final def zone: Zone = {
    With.geography.zoneByTile(this)
  }
  @inline final def base: Option[Base] = {
    With.geography.baseByTile(this)
  }
  @inline final def groundPixels(other: Pixel): Double = {
    With.paths.groundPixels(pixelCenter, other)
  }
  @inline final def groundPixels(other: Tile): Double = {
    With.paths.groundPixels(pixelCenter, other.pixelCenter)
  }
  @inline final def travelPixelsFor(other: Pixel, unit: UnitInfo): Double = {
    unit.pixelDistanceTravelling(pixelCenter, other)
  }
  @inline final def travelPixelsFor(other: Tile, unit: UnitInfo): Double = {
    unit.pixelDistanceTravelling(this, other)
  }
  @inline final def altitude: Int = {
    With.game.getGroundHeight(x, y)
  }
  @inline final def altitudeUnchecked: Int = {
    With.game.getGroundHeight(x, y) // TODO: Replace with actually unchecked variant
  }
  @inline final def toRectangle: TileRectangle = {
    TileRectangle(this, this.add(1, 1))
  }
  @inline final def walkable: Boolean = {
    valid && walkableUnchecked
  }
  @inline final def walkableUnchecked: Boolean = {
    With.grids.walkable.getUnchecked(i)
  }
  @inline final def traversableBy(unit: UnitInfo): Boolean = {
    unit.flying || walkable
  }
  @inline final def nearestTraversableBy(unit: UnitInfo): Tile = {
    if (unit.flying) this else pixelCenter.nearestTraversableBy(unit).tile
  }
  @inline final def nearestWalkableTile: Tile = {
    if (walkable) this else Spiral.points(16).view.map(add).find(_.walkable).getOrElse(this)
  }
  @inline final def buildable: Boolean = {
    valid && buildableUnchecked
  }
  @inline final def buildableUnchecked: Boolean = {
    With.grids.buildable.getUnchecked(i)
  }
  @inline final def explored: Boolean = {
    With.game.isExplored(x, y)
  }
  @inline final def visible: Boolean = {
    valid && visibleUnchecked
  }
  @inline final def visibleUnchecked: Boolean = {
    With.game.isVisible(x, y) // TODO: Replace with actually unchecked variant
  }
  @inline final def visibleToEnemy: Boolean = {
    valid && visibleToEnemyUnchecked
  }
  @inline final def visibleToEnemyUnchecked: Boolean = {
    With.grids.enemyVision.isSetUnchecked(i)
  }
  @inline final def friendlyDetected: Boolean = {
    valid && friendlyDetectedUnchecked
  }
  @inline final def friendlyDetectedUnchecked: Boolean = {
    With.grids.friendlyDetection.isSetUnchecked(i)
  }
  @inline final def enemyDetected: Boolean = {
    valid && enemyDetectedUnchecked
  }
  @inline final def enemyDetectedUnchecked: Boolean = {
    With.grids.enemyDetection.isDetectedUnchecked(i)
  }
  @inline final def visibleBwapi: Boolean = {
    With.game.isVisible(x, y)
  }
  @inline final def scoutingPathDistanceBases: Int = {
    With.grids.scoutingPathsBases.get(this)
  }
  @inline final def scoutingPathDistanceStartLocations: Int = {
    With.grids.scoutingPathsStartLocations.get(this)
  }
  @inline final def creep: Boolean = {
    With.game.hasCreep(x, y)
  }
  @inline final def creepUnchecked: Boolean = {
    With.game.hasCreep(x, y) // TODO: Replace with actually unchecked variant
  }
}
