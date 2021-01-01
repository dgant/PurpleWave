package Planning.Plans.Placement

import Lifecycle.With
import Mathematics.Points.{Point, Tile}
import Mathematics.Shapes.Spiral
import Performance.Cache
import Planning.Plan
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClasses.UnitClass
import Utilities.Forever

import scala.collection.mutable

class PlaceFFE extends Plan {

  class Wall {
    var tight: Boolean = false
    var gapSize: Int = 0
  }

  class WallGeography {
  }

  object WallDirection extends Enumeration {
    type WallDirection = Value
    val Horizontal, Vertical, Diagonal = Value
  }

  def createWall(): Option[Wall] = {
    // Logic adapted from Locutus!
    // https://github.com/bmnielsen/Locutus/blob/4ad67df04129c8a28c833b5b4e667632e7dec72f/Steamhammer/Source/LocutusWall.cpp

    // Use precalculated wall?
    // TODO

    def validWalkable(tile: Tile): Boolean = tile.valid && With.grids.walkableTerrain.get(tile)

    if (With.geography.ourNatural.zone.exit.isEmpty) return None
    val naturalBase     = With.geography.ourNatural
    val naturalZone     = naturalBase.zone
    val naturalExit     = naturalZone.exit.get
    val naturalSideRaw1 = naturalZone.exit.get.sidePixels.head.tileIncluding
    val naturalSideRaw2 = naturalZone.exit.get.sidePixels.last.tileIncluding
    val naturalHomePost = naturalExit.pixelCenter.multiply(3).add(naturalBase.townHallArea.midPixel).divide(4).tileIncluding
    val naturalGoalPost = naturalExit.pixelCenter.project(naturalBase.townHallArea.midPixel, - 5 * 32).tileIncluding
    val naturalHomeTile = Spiral.points(6).map(naturalHomePost.add).filter(_.zone == naturalZone).find(validWalkable)
    val naturalGoalTile = Spiral.points(6).map(naturalGoalPost.add).filterNot(_.zone == naturalZone).find(validWalkable)
    val naturalAltitude = naturalBase.townHallTile.altitude
    val geyserTiles     = naturalBase.gas.map(_.tileArea.tiles).reduceLeft(_ ++ _)
    val exitDeltaX      = Math.abs(naturalSideRaw1.x - naturalSideRaw2.x)
    val exitDeltaY      = Math.abs(naturalSideRaw1.y - naturalSideRaw2.y)
    val wallDirection   = if (exitDeltaY <= 2) WallDirection.Horizontal else if (exitDeltaX <= 2) WallDirection.Vertical else WallDirection.Diagonal
    val sidesForwards   = (naturalSideRaw1, naturalSideRaw2)
    val sidesBackwards  = (naturalSideRaw2, naturalSideRaw1)
    val (naturalSide1, naturalSide2) = if (wallDirection match {
      case WallDirection.Horizontal | WallDirection.Diagonal =>
        naturalSideRaw1.x < naturalSideRaw2.x
      case WallDirection.Diagonal =>
        naturalSideRaw1.y < naturalSideRaw2.y
    }) sidesForwards else sidesBackwards

    val adjacentUp    = (-1 to 1).map(Point(_, -1))
    val adjacentDown  = (-1 to 1).map(Point(_, 1))
    val adjacentLeft  = (-1 to 1).map(Point(-1, _))
    val adjacentRight = (-1 to 1).map(Point(1, _))
    def invalidOrUnwalkableFully(from: Tile, to: Tile): Boolean = invalidOrUnwalkablePartly(to) // TODO: Walkposition-resolution walkability
    def invalidOrUnwalkablePartly(tile: Tile): Boolean = ! tile.valid || With.grids.walkable.get(tile)
    def designWall(tight: Boolean, maxGapSize: Int = 256): Option[Wall] = {
      // DesignWall part 1: Identify places each building can go
      val optimalPathLength = 256 // TODO
      val side1Tiles, side2Tiles, side1ForgeTiles, side2ForgeTiles, side1GatewayTiles, side2GatewayTiles = new mutable.HashSet[Tile]
      def addPlacementOptions(tile: Tile, unitClass: UnitClass, sideTiles: mutable.Set[Tile], buildingTiles: mutable.Set[Tile]): Unit = {
        if ( ! tile.valid) return
        if ( ! tile.walkable) sideTiles += tile
        if (tile.altitude != naturalAltitude) return
        val isForge = unitClass == Protoss.Forge
        val isGate = unitClass == Protoss.Gateway
        val geyserBlocksUp    = adjacentUp    .map(tile.add).exists(geyserTiles.contains)
        val geyserBlocksDown  = adjacentDown  .map(tile.add).exists(geyserTiles.contains)
        val geyserBlocksLeft  = adjacentLeft  .map(tile.add).exists(geyserTiles.contains)
        val geyserBlocksRight = adjacentRight .map(tile.add).exists(geyserTiles.contains)
        val blockedUp     = geyserBlocksUp    || (isForge && invalidOrUnwalkableFully(tile, tile.up))     || ( ! tight && invalidOrUnwalkablePartly(tile.up))
        val blockedDown   = geyserBlocksDown  || (isGate  && invalidOrUnwalkableFully(tile, tile.down))   || ( ! tight && invalidOrUnwalkablePartly(tile.down))
        val blockedLeft   = geyserBlocksLeft  || (isForge && invalidOrUnwalkableFully(tile, tile.left))   || ( ! tight && invalidOrUnwalkablePartly(tile.left))
        val blockedRight  = geyserBlocksRight || (isGate  && invalidOrUnwalkableFully(tile, tile.right))  || ( ! tight && invalidOrUnwalkablePartly(tile.left))
        def addTile(tile: Tile): Unit = buildingTiles.add(tile)
        if (blockedUp)    (0 until unitClass.tileWidth)   .foreach(dx => addTile(tile.add(-dx,                      0)))
        if (blockedDown)  (0 until unitClass.tileWidth)   .foreach(dx => addTile(tile.add(-dx,                      1 - unitClass.tileHeight)))
        if (blockedLeft)  (0 until unitClass.tileHeight)  .foreach(dy => addTile(tile.add(0,                        -dy)))
        if (blockedRight) (0 until unitClass.tileHeight)  .foreach(dy => addTile(tile.add(1 - unitClass.tileWidth,  -dy)))
      }
      def considerPlacementDelta1(delta: Point): Unit = {
        val testTile1 = naturalSide1.add(delta)
        addPlacementOptions(testTile1, Protoss.Forge,   side1Tiles, side1ForgeTiles)
        addPlacementOptions(testTile1, Protoss.Gateway, side1Tiles, side1GatewayTiles)
      }
      def considerPlacementDelta2(delta: Point): Unit = {
        val testTile2 = naturalSide2.add(delta)
        addPlacementOptions(testTile2, Protoss.Forge,   side2Tiles, side2ForgeTiles)
        addPlacementOptions(testTile2, Protoss.Gateway, side2Tiles, side2GatewayTiles)
      }
      def considerPlacementDelta(delta: Point): Unit = {
        considerPlacementDelta1(delta)
        considerPlacementDelta2(delta)
      }
      wallDirection match {
        case WallDirection.Horizontal =>
          (-2 to 2)
            .flatMap(dx => (-5 to 5)
            .map(Point(dx, _)))
            .foreach(considerPlacementDelta)
        case WallDirection.Vertical =>
          (-5 to 5)
            .flatMap(dx => (-2 to 2)
            .map(Point(dx, _)))
            .foreach(considerPlacementDelta)
        case WallDirection.Diagonal =>
          // We know the slope is defined because we wouldn't be diagonal otherwise
          val slope = (naturalSide2.y - naturalSide1.y).toDouble / (naturalSide2.x - naturalSide1.x)
          (-4 to 4)
            .flatMap(dx => (-3 to 3).map(_ + Math.round(dx * slope).toInt)
            .map(Point(dx, _)))
            .foreach(point => {
              val tile1 = naturalSide1.add(point)
              val tile2 = naturalSide2.add(point)
              if (tile1.tileDistanceSquared(naturalSide1) <= tile1.tileDistanceSquared(naturalSide2)) {
                considerPlacementDelta1(point)
              }
              if (tile2.tileDistanceSquared(naturalSide2) <= tile2.tileDistanceSquared(naturalSide1)) {
                considerPlacementDelta2(point)
              }
            })
      }

      // DesignWall part 2: Construct all possible wall combinations
      case class WallOption(forgeTile: Tile, gatewayTile: Tile)
      case class WallOptionResult(var legal: Boolean = false)
      val wallOptions = new mutable.HashMap[WallOption, WallOptionResult]
      def addWallOption(forgeTile: Tile, gatewayTile: Tile): Unit = {
        val option = WallOption(forgeTile, gatewayTile)
        // Don't repeat a wall
        if (wallOptions.contains(option))
        wallOptions(option) = WallOptionResult()

        val areaForge = Protoss.Forge.tileArea.add(forgeTile)
        val areaGate  = Protoss.Gateway.tileArea.add(gatewayTile)
        val tilesAll  = areaForge.tiles ++ areaGate.tiles
        // Don't overlap buildings
        if (areaForge.intersects(areaGate)) return

        // Reject obviously bad placements
        if (tilesAll.exists( ! _.valid)) return
        if (tilesAll.exists( ! With.grids.buildable.get(_))) return

        // TODO -- WELCOME BACK FROM LA!
        // 1. Wrapping up the implementation of addWallOption()
        // 2. What's left after that is picking the best wall
        // 3. Then issuing a placement request
      }

      None
    }
    val wall1 = designWall(tight = true).orElse(designWall(tight = false))
    val wall2 = if (wall1.exists(w => w.tight && w.gapSize >= 5)) designWall(tight = false, maxGapSize = wall1.get.gapSize - 3) else wall1
    val wall = wall2.orElse(wall1)

    wall
  }

  val wall = new Cache(() => createWall(), Forever())
  override def onUpdate(): Unit = {

  }
}
