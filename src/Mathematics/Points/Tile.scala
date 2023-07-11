package Mathematics.Points

import Information.Geography.Types.{Base, Metro, Zone}
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Shapes.Spiral
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.?
import bwapi.TilePosition

final case class Tile(argX: Int, argY: Int) extends AbstractPoint(argX, argY) {
  
  def this(i: Int) = this(i % With.mapTileWidth, i / With.mapTileWidth )
  def this(tilePosition: TilePosition) = this(tilePosition.getX, tilePosition.getY)

  lazy val i: Int = x + With.mapTileWidth * y

  @inline def bwapi: TilePosition = new TilePosition(x, y)
  
  // Performance optimization: This is not a strict equality check!
  // If you try to compare a Tile to a non-Tile you can get incorrect results.
  // Invalid tiles can also produce incorrect results.
  // For example, on a 256 x 256 map: (0, 1) == (-256, 0)
  @inline override def equals(other: scala.Any): Boolean = hashCode == other.hashCode
  @inline override def hashCode: Int = i
  
  @inline def valid: Boolean = {
    x >= 0 &&
    y >= 0 &&
    x < With.mapTileWidth &&
    y < With.mapTileHeight
  }
  @inline def clip: Tile = Tile(
    Maff.clamp(x, 0, With.mapTileWidth - 1),
    Maff.clamp(y, 0, With.mapTileHeight - 1))

  @inline def tileDistanceFromEdge: Int = {
    var min = x
    if (y < min) min = y
    if (With.mapTileWidth  - x < min) min = With.mapTileWidth - x
    if (With.mapTileHeight - y < min) min = With.mapTileHeight - y
    min
  }

  @inline def add(dx: Int, dy: Int): Tile = {
    Tile(x + dx, y + dy)
  }
  @inline def add(point: Point): Tile = {
    add(point.x, point.y)
  }
  @inline def add(tile: Tile): Tile = {
    add(tile.x, tile.y)
  }
  @inline def subtract(dx: Int, dy: Int): Tile = {
    add(-dx, -dy)
  }
  @inline def subtract(tile: Tile): Tile = {
    subtract(tile.x, tile.y)
  }
  @inline def multiply(scale: Int): Tile = {
    Tile(scale * x, scale * y)
  }
  @inline def divide(scale: Int): Tile = {
    Tile(x / scale, y / scale)
  }
  @inline def midpoint(pixel: Tile): Tile = {
    add(pixel).divide(2)
  }
  @inline def pixelDistance(tile: Tile): Double = {
    center.pixelDistance(tile.center)
  }
  @inline def pixelDistance(pixel: Pixel): Double = {
    center.pixelDistance(pixel)
  }
  @inline def groundPixels(other: Pixel): Double = {
    With.paths.groundPixels(center, other)
  }
  @inline def groundPixels(other: Tile): Double = {
    With.paths.groundPixels(center, other.center)
  }
  @inline def groundTiles(other: Tile): Int = {
    With.paths.groundTilesManhattan(this, other)
  }
  @inline def groundTiles(other: Pixel): Int = {
    With.paths.groundTilesManhattan(this, other.tile)
  }
  @inline def travelPixelsFor(other: Pixel, unit: UnitInfo): Double = {
    unit.pixelDistanceTravelling(center, other)
  }
  @inline def travelPixelsFor(other: Tile, unit: UnitInfo): Double = {
    unit.pixelDistanceTravelling(this, other)
  }
  @inline def radiateRadians(angleRadians: Double, tiles: Double): Tile = {
    add(
      (tiles * Math.cos(angleRadians)).toInt,
      (tiles * Math.sin(angleRadians)).toInt)
  }
  @inline def degreesTo(other: Tile): Double = {
    radiansTo(other) * Maff.x2PiInv360
  }
  @inline def radiansTo(other: Tile): Double = {
    Maff.fastAtan2(other.y - y, other.x - x)
  }
  @inline def radiansToSlow(other: Tile): Double = {
    Math.atan2(other.y - y, other.x - x)
  }
  @inline def directionTo(other: Tile): Direction = {
    other.subtract(this).direction
  }
  @inline def tileDistanceManhattan(tile: Tile): Int = {
    Math.abs(x-tile.x) + Math.abs(y-tile.y)
  }
  @inline def tileDistanceChebyshev(tile: Tile): Int = {
    Math.max(Math.abs(x-tile.x), Math.abs(y-tile.y))
  }
  @inline def tileDistanceFast(tile: Tile): Double = {
    Maff.broodWarDistance(x, y, tile.x, tile.y)
  }
  @inline def tileDistanceSquared(tile: Tile): Int = {
    Maff.squared(x - tile.x) + Maff.squared(y - tile.y)
  }
  @inline def topLeftPixel: Pixel = {
    Pixel(Maff.x32(x), Maff.x32(y))
  }
  @inline def topRightPixel: Pixel = {
    Pixel(Maff.x32(x) + 31, Maff.x32(y))
  }
  @inline def bottomRightPixel: Pixel = {
    Pixel(Maff.x32(x) + 31, Maff.x32(y) + 31)
  }
  @inline def bottomLeftPixel: Pixel = {
    Pixel(Maff.x32(x), Maff.x32(y) + 31)
  }
  @inline def pixelCorners: Array[Pixel] = {
    Array(topLeftPixel, topRightPixel, bottomRightPixel, bottomLeftPixel)
  }
  @inline def center: Pixel = {
    Pixel(Maff.x32(x) + 15, Maff.x32(y) + 15)
  }
  @inline def contains(pixel: Pixel): Boolean = {
    x == Maff.div32(pixel.x) && y == Maff.div32(pixel.y)
  }
  @inline def topLeftWalk: WalkTile = {
    WalkTile(Maff.x4(x), Maff.x4(y))
  }
  @inline def left: Tile = {
    Tile(x - 1, y)
  }
  @inline def right: Tile = {
    Tile(x + 1, y)
  }
  @inline def up: Tile = {
    Tile(x, y - 1) // Remember our flipped coordinate system
  }
  @inline def down: Tile = {
    Tile(x, y + 1) // Remember our flipped coordinate system
  }
  @inline def adjacent4: Array[Tile] = {
    Array(up, down, left, right)
  }
  @inline def adjacent5: Array[Tile] = {
    Array(this, up, down, left, right)
  }
  @inline def adjacent8: Array[Tile] = {
    Array(up, down, left, right, up.left, up.right, down.left, down.right)
  }
  @inline def adjacent9: Array[Tile] = {
    Array(this, up, down, left, right, up.left, up.right, down.left, down.right)
  }
  @inline def metro: Option[Metro] = {
    zone.metro
  }
  @inline def zone: Zone = {
    With.geography.zoneByTile(this)
  }
  @inline def base: Option[Base] = {
    With.geography.baseByTile(this)
  }
  @inline def mobility: Int = {
    With.grids.mobilityTerrain(this)
  }
  @inline def altitude: Int = {
    With.game.getGroundHeight(x, y)
  }
  @inline def altitudeUnchecked: Int = {
    With.game.getGroundHeight(x, y) // TODO: Replace with actually unchecked variant
  }
  @inline def toRectangle: TileRectangle = {
    TileRectangle(this, this.add(1, 1))
  }
  @inline def walkable: Boolean = {
    valid && walkableUnchecked
  }
  @inline def walkableUnchecked: Boolean = {
    With.grids.walkable.getUnchecked(i)
  }
  @inline def walkableTerrain: Boolean = {
    valid && walkableTerrainUnchecked
  }
  @inline def walkableTerrainUnchecked: Boolean = {
    With.grids.walkableTerrain.getUnchecked(i)
  }
  @inline def traversableBy(unit: UnitInfo): Boolean = {
    unit.flying || walkable
  }
  @inline def traversableTile(unit: UnitInfo): Tile = {
    if (unit.flying) this else center.traversiblePixel(unit).tile
  }
  @inline def walkableTile: Tile = {
    ?(walkable, this, Spiral(16).map(add).find(_.walkable).getOrElse(this))
  }
  @inline def buildable: Boolean = {
    valid && buildableUnchecked
  }
  @inline def buildableUnchecked: Boolean = {
    With.grids.buildable.getUnchecked(i)
  }
  @inline def buildableTerrain: Boolean = {
    With.game.isBuildable(x, y)
  }
  @inline def buildableTerrainUnchecked: Boolean = {
    With.grids.buildableTerrain.getUnchecked(this)
  }
  @inline def explored: Boolean = {
    With.game.isExplored(x, y)
  }
  @inline def exploredUnchecked: Boolean = {
    With.game.isExploredUnsafe(x, y)
  }
  @inline def visible: Boolean = {
    valid && visibleUnchecked
  }
  @inline def visibleUnchecked: Boolean = {
    With.game.isVisibleUnsafe(x, y)
  }
  @inline def visibleToEnemy: Boolean = {
    valid && visibleToEnemyUnchecked
  }
  @inline def visibleToEnemyUnchecked: Boolean = {
    With.grids.enemyVision.inRangeUnchecked(i)
  }
  @inline def friendlyDetected: Boolean = {
    valid && friendlyDetectedUnchecked
  }
  @inline def friendlyDetectedUnchecked: Boolean = {
    With.grids.friendlyDetection.inRangeUnchecked(i)
  }
  @inline def enemyDetected: Boolean = {
    valid && enemyDetectedUnchecked
  }
  @inline def enemyDetectedUnchecked: Boolean = {
    With.grids.enemyDetection.inRangeUnchecked(i)
  }
  @inline def enemyRange: Int = {
    With.grids.enemyRangeAirGround(this)
  }
  @inline def enemyRangeGround: Int = {
    With.grids.enemyRangeGround(this)
  }
  @inline def enemyRangeGroundUnchecked: Int = {
    With.grids.enemyRangeGround.getUnchecked(this)
  }
  @inline def enemyRangeAir: Int = {
    With.grids.enemyRangeAir(this)
  }
  @inline def enemyRangeAirUnchecked: Int = {
    With.grids.enemyRangeAir.getUnchecked(this)
  }
  @inline def enemyRangeAgainst(unit: UnitInfo): Int = {
    if (unit.flying) enemyRangeAir else enemyRangeGround
  }
  @inline def enemyVulnerabilityGround: Int = {
    With.grids.enemyVulnerabilityGround(this)
  }
  @inline def enemyDpfGround: Double = {
    With.grids.enemyRangeGround.dpfGround(this)
  }
  @inline def enemyDpfAir: Double = {
    With.grids.enemyRangeAir.dpfAir(this)
  }
  @inline def enemyDamageGround: Double = {
    With.grids.enemyRangeGround.damageGround(this)
  }
  @inline def enemyDamageAir: Double = {
    With.grids.enemyRangeAir.damageAir(this)
  }
  @inline def friendlyDpfGround: Double = {
    With.grids.enemyVulnerabilityGround.dpfGround(this)
  }
  @inline def friendlyDamageGround: Double = {
    With.grids.enemyVulnerabilityGround.damageGround(this)
  }
  @inline def lastSeen: Int = {
    With.grids.lastSeen(this)
  }
  @inline def units: Traversable[UnitInfo] = {
    With.grids.units.get(this)
  }
  @inline def scoutingPathDistanceBases: Int = {
    With.grids.scoutingPathsBases.get(this)
  }
  @inline def scoutingPathDistanceStartLocations: Int = {
    With.grids.scoutingPathsStartLocations.get(this)
  }
  @inline def creep: Boolean = {
    With.game.hasCreep(x, y)
  }
  @inline def creepUnchecked: Boolean = {
    With.game.hasCreepUnsafe(x, y)
  }
  @inline def enemiesVulnerableGround: Seq[UnitInfo] = {
    With.grids.enemyVulnerabilityGround.unitsOn(i)
  }
  @inline def enemiesAttackingGround: Seq[UnitInfo] = {
    With.grids.enemyRangeGround.unitsOn(i)
  }
  @inline def enemiesNearlyExposedGround: Seq[UnitInfo] = {
    With.grids.enemyVulnerabilityGround.unitsNear(i)
  }
  @inline def enemiesNearlyAttackingGround: Seq[UnitInfo] = {
    With.grids.enemyRangeGround.unitsNear(i)
  }
  @inline def enemiesAttackingAir: Seq[UnitInfo] = {
    With.grids.enemyRangeAir.unitsOn(i)
  }
  @inline def enemiesNearlyAttackingAir: Seq[UnitInfo] = {
    With.grids.enemyRangeAir.unitsNear(i)
  }
  @inline def enemiesAttacking(friendly: FriendlyUnitInfo): Seq[UnitInfo] = {
    ?(friendly.flying, enemiesAttackingAir, enemiesAttackingGround)
  }
  @inline def enemiesNearlyAttacking(friendly: FriendlyUnitInfo): Seq[UnitInfo] = {
    ?(friendly.flying, enemiesNearlyAttackingAir, enemiesNearlyAttackingGround)
  }
}
