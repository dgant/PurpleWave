package Placement.Walls

import Information.Geography.Pathfinding.PathfindProfile
import Information.Geography.Types.{Edge, Zone}
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Point, Tile, TileRectangle}
import Mathematics.Shapes.{Pylons, Rectangle, RoundedBox}
import Placement.Generation.TileGeneratorRectangularSweep
import Placement.Walls.WallFillers.{PylonsCannons, WallFiller}
import Placement.Walls.WallSpans.{TerrainGas, TerrainHall}
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClasses.UnitClass
import Utilities.?

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class WallFinder(zone: Zone, exit: Edge, entrance: Tile, constraints: Seq[WallConstraint], filler: WallFiller) {
  val metrics                         = new WallMetrics
  val wallScores                      = new ArrayBuffer[(Wall, Double)]()
  val incompleteWall  : Wall          = new Wall
  var wall            : Option[Wall]  = None

  def generate(): Option[Wall] = {
    placeRequiredBuildings()
    if (wall.isEmpty) return None
    placeFiller()
    wall
  }

  def placeRequiredBuildings(): Option[Wall] = {
    if (constraints.isEmpty)        return None
    if (zone.exitOriginal.isEmpty)  return None
    var constraintIndex : Int   = 0
    var gapsLeft        : Int   = 0

    def constraint: WallConstraint = incompleteWall.constraint
    def pickNextConstraint(): Unit = {
      incompleteWall.buildings.clear()
      incompleteWall.constraint = constraints(constraintIndex)
      incompleteWall.gap        = None
      incompleteWall.hallway    = Seq.empty
      constraintIndex += 1
      gapsLeft = constraint.gapTiles
    }

    // Overall plan:
    // - Identify starting boundary
    // - Identify ending boundaries
    //   - Terrain-Terrain
    //   - Terrain-Gas
    //   - Terrain-Hall
    // For each constraint:
    // - For each starting boundary:
    //  - For each ending boundary:
    //    - If this line contains any other starting/ending boundaries, skip
    //    - If this line's max width exceeds the max width of our buildings + gap size, skip
    //    - If this line's max height exceeds the max height of our buildings + gap size, skip
    //    - Fill queue with buildings to place
    //    - For each place we could place the first building adjacent to the first tile: (LATER: Select cleverly to fill gaps)
    //      - Try placing each building adjacent to it or using our gap budget
    def boundaryEnd = constraint.span match {
      case TerrainGas   => zone.wallPerimeterGas;
      case TerrainHall  => zone.wallPerimeterHall;
      case _            => zone.wallPerimeterExit
    }

    def tryPlace(horizon: Seq[Tile], buildingsLeft: Seq[UnitClass], gapsLeft: Int): Boolean = {
      // Check if a complete placement is valid.
      if (buildingsLeft.isEmpty) {
        // TODO: If gapsLeft is zero enforce constraint.blocksUnit

        // Require the gap to meet constraints
        if (horizon.forall(t => boundaryEnd.forall(_.tileDistanceManhattan(t) > 1 + gapsLeft))) {
          metrics.gapTooWide += 1
          return false
        }

        /* Disabling because we're changing the way we iterate to avoid having to do this check, at a cost of rejecting walls where the gap is at the far end
        // Use Chebyshev distance to check gap size because cater-corner diagonals are not acceptable
        if (horizon.exists(t => boundaryEnd.exists(_.tileDistanceChebyshev(t) <= gapsLeft))) {
          metrics.gapTooNarrow += 1
          return false
        }
        */
        // If the wall has a Pylon, then it must power the other buildings.
        val pylons = incompleteWall.buildings.filter(_._2.powers)
        if (pylons.nonEmpty) {
          val unpowered = incompleteWall.buildings.find(b => b._2.requiresPsi && ! pylons.exists(p => Pylons.powers(p._1, b._1, b._2)))
          if (unpowered.isDefined) {
            metrics.unpowered += 1
            return false
          }
        }
        wallScores += ((new Wall(incompleteWall), scoreWall(incompleteWall)))
        return true
      }

      // For each tile in the boundary,
      //   for each way to place a tile adjacent to it,
      //     for each gap size still available,
      //       Try placing the next building there,
      //       then repeat recursively.
      //       If we're out of buildings to place,
      //       and the whole arrangement is legal,
      //       publish it.
      //
      // Places around the target tile to try at each gap size:
      //    1111
      //    0000
      //  10----01
      //  10----01
      //  10---+01
      //    0000
      //    1111
      //
      val building  = buildingsLeft.head
      val width     = building.tileWidthPlusAddon
      val height    = building.tileHeight
      horizon.exists(horizonTile => {
        metrics.tilesConsidered += 1
        RoundedBox(width + 2, height + 2).exists(relative =>
          (buildingsLeft.length - 1 to gapsLeft).exists(gapsUsed => {
            val gapOffset =
                    if (relative.x == 0)          Point(- gapsUsed,   0)
              else  if (relative.y == 0)          Point(  0,        - gapsUsed)
              else  if (relative.x == width + 1)  Point(  gapsUsed,   0)
              else                                Point(  0,          gapsUsed)
            val buildingTile = horizonTile
              .subtract(width, height)
              .add(relative)
              .add(gapOffset)
            lazy val (alleyLeft, alleyRight, alleyUp, alleyDown) = incompleteWall.buildings.lastOption.map(b => {
              val lastBuilding  = b._2
              val lastTile      = b._1
              val tileGapLeft   = buildingTile.x  - lastTile.x      - lastBuilding.tileWidthPlusAddon
              val tileGapRight  = lastTile.x      - buildingTile.x  - width
              val tileGapUp     = buildingTile.y  - lastTile.y      - lastBuilding.tileHeight
              val tileGapDown   = lastTile.y      - buildingTile.y  - height
              val alleyLeft     = if (tileGapLeft   == 0) lastBuilding.marginRightInclusive   + building.marginLeft           else -1
              val alleyRight    = if (tileGapRight  == 0) lastBuilding.marginLeft             + building.marginRightInclusive else -1
              val alleyUp       = if (tileGapUp     == 0) lastBuilding.marginDownInclusive    + building.marginUp             else -1
              val alleyDown     = if (tileGapDown   == 0) lastBuilding.marginUp               + building.marginDownInclusive  else -1
              (alleyLeft, alleyRight, alleyUp, alleyDown)
            }).getOrElse((-1, -1, -1, -1))

            if ( ! canPlace(width, height, buildingTile, countMetrics = true)) {
              false
            }

            // First building must touch the target zone
              /*
            else if (incompleteWall.buildings.isEmpty && Corners(width, height).map(buildingTile.add).forall(_.zone != zone)) {
              metrics.wrongZone += 1
              false
            }
            */

            // Gaps must meet tightness criterion
            else if (gapsUsed == 0 && (alleyLeft >= constraint.blocksUnit.width || alleyRight >= constraint.blocksUnit.width || alleyUp >= constraint.blocksUnit.height || alleyDown >= constraint.blocksUnit.height)) {
              metrics.insufficientlyTight += 1
              false
            }

            // Success! Place the next building.
            // Apply changes
            else {
              incompleteWall.buildings += ((buildingTile, building))
              val gapBefore = incompleteWall.gap
              if (gapBefore.isEmpty && gapsUsed > 0) {
                incompleteWall.gap = Some(horizonTile.add(gapOffset.direction.x, gapOffset.direction.y))
              }
              // Test changes
              val output = tryPlace(building.tileArea.tilesAtEdge.map(buildingTile.add), buildingsLeft.drop(1), gapsLeft - gapsUsed)
              if ( ! output) {
                metrics.failedRecursively += 1
              }
              // Revert changes
              incompleteWall.gap = gapBefore
              incompleteWall.buildings.remove(incompleteWall.buildings.length - 1)
              output
            }}))})
    }

    while (constraintIndex < constraints.length && wall.isEmpty) {
      pickNextConstraint()
      val permutations = constraint.buildings.permutations.toVector.distinct
      metrics.permutations += permutations.length
      permutations.foreach(tryPlace(zone.wallPerimeterEntrance, _, constraint.gapTiles))
      metrics.acceptable = wallScores.length
      wall = Maff.minBy(wallScores)(_._2).map(_._1)
    }
    wall
  }

  def canPlace(width: Int, height: Int, buildingTile: Tile, countMetrics: Boolean): Boolean = {
    val buildingTiles = Rectangle(width, height).map(buildingTile.add)
    val buildingEndX  = buildingTile.x + width
    val buildingEndY  = buildingTile.y + height
    val metricsValue  = Maff.fromBoolean(countMetrics)

    // Terrain must be buildable
    if ( ! With.grids.buildableW(width)(buildingTile)) {
      metrics.unbuildableTerrain += metricsValue
      return false
    }

    // Buildings can't intersect
    if (incompleteWall.buildings.exists(existingBuilding =>
      Maff.rectanglesIntersect(
        buildingTile.x,
        buildingTile.y,
        buildingEndX,
        buildingEndY,
        existingBuilding._1.x,
        existingBuilding._1.y,
        existingBuilding._1.x + existingBuilding._2.tileWidthPlusAddon,
        existingBuilding._1.y + existingBuilding._2.tileHeight))) {
      metrics.intersectsPrevious += metricsValue
      return false
    }

    // Buildings can't block town hall
    if (buildingTile.zone.bases.exists(b => Maff.rectanglesIntersect(buildingTile.x, buildingTile.y, buildingEndX, buildingEndY,
      b.townHallArea.startInclusive.x, b.townHallArea.startInclusive.y,
      b.townHallArea.endExclusive.x, b.townHallArea.endExclusive.y))) {
      metrics.intersectsHall += metricsValue
      return false
    }

    // Buildings must not block mining
    if(buildingTiles.exists(t => t.base.exists(_.resourcePathTiles.contains(t)))) {
      metrics.intersectsMining += metricsValue
      return false
    }

    // Tiles must be buildable (eg. not blocked by neutrals)
    if (buildingTiles.exists( ! _.buildable)) {
      metrics.unbuildableGranular += metricsValue
      return false
    }

    true
  }

  def scoreWall(wall: Wall): Double = {
    if (wall.buildings.isEmpty) return 0.0
    val endTiles  = wall.buildings.map(p => p._1.add(p._2.tileWidthPlusAddon, p._2.tileHeight))
    val xMin      = wall.buildings.map(_._1.x).min
    val yMin      = wall.buildings.map(_._1.y).min
    val xMax      = endTiles.map(_.x).max
    val yMax      = endTiles.map(_.y).max
    val scoreArea = Maff.broodWarDistance(xMin, yMin, xMax, yMax)
    scoreArea
  }

  def placeFiller(): Unit = {
    if (wall.get.gap.isDefined) {
      val gap             = wall.get.gap.get
      val hallwayWalls    = (wall.get.buildings.flatMap(p => p._2.tileArea.add(p._1).tiles) ++ zone.bases.flatMap(_.townHallArea.tiles)).toSet
      val hallwayProfile  = new PathfindProfile(gap, Some(entrance), employGroundDist = true, alsoUnwalkable = hallwayWalls, allowDiagonals = false)
      val hallwayPath     = hallwayProfile.find.tiles
      wall.get.hallway    = hallwayPath.getOrElse(Seq.empty)
      // We can't safely fill without preserving a path
      if (wall.get.hallway.isEmpty) {
        metrics.noPath += 1
        wall = None
        return
      }
    }

    if (filler == PylonsCannons) fill2X2()
  }

  def fill2X2(): Unit = {
    val offsets = Vector(Point(0, 0), Point(0, 1), Point(1, 0), Point(1, 1))
    val placements = new mutable.HashMap[Point, ArrayBuffer[Tile]]
    offsets.foreach(offset => {
      placements(offset) = new ArrayBuffer[Tile]()
      fill2x2Offset(offset, placements(offset))
    })
    val tiles = placements.values.maxBy(_.length) // TODO: Also score based on quality of fit
    if (filler == PylonsCannons) {
      if (tiles.length + wall.get.buildings.length < 7) {
        metrics.insufficientFiller += 1
        wall = None
        return
      }
      val centroid          = Maff.centroid(wall.get.buildings.map(b => b._1.topLeftPixel.add(b._2.dimensionLeft, b._2.dimensionUp)))
      wall.get.buildings  ++= tiles.map(t => (t, Protoss.PhotonCannon))
      def pylons            = wall.get.buildings.view.filter(b => b._2.powers)
      def convertable       = wall.get.buildings.view.filter(b => b._2 == Protoss.PhotonCannon)
      def unpowered         = wall.get.buildings.view.filter(b => b._2.requiresPsi && ! pylons.exists(p => Pylons.powers(p._1, b._1, b._2)))
      while (unpowered.nonEmpty && convertable.nonEmpty) {
        val scores = convertable.map(c => (c._1, c._2, unpowered.map(u => ?(Pylons.powers(c._1, u._1, u._2), ?(u._2 == Protoss.PhotonCannon, 1, 100), 0)).sum))
        val maxScore = scores.map(_._3).max
        if (maxScore == 0) {
          metrics.unpowered += 1
          wall = None
          return
        }
        val candidates  = scores.filter(_._3 == maxScore)
        val best        = candidates.maxBy(_._1.topLeftPixel.add(32, 32).pixelDistanceSquared(centroid))
        wall.get.buildings -= ((best._1, best._2))
        wall.get.buildings += ((best._1, Protoss.Pylon))
        if (unpowered.exists(_._2.tileWidth > 2)) {
          metrics.unpowered += 1
          wall = None
          return
        }
      }
    }
  }

  def fill2x2Offset(offset: Point, placements: ArrayBuffer[Tile]): Unit = {
    val w = wall.get
    val generator = new TileGeneratorRectangularSweep(exit.pixelCenter.tile, zone.boundary.startInclusive, zone.boundary.endExclusive, exit.pixelCenter.directionTo(zone.heart.center))
    while (generator.hasNext && placements.length < 16) {
      val tile = generator.next()
      val rectangle = new TileRectangle(tile, 2, 2)
      if (tile.zone == zone
        // Is legal placement
        && canPlace(2, 2, tile, countMetrics = false)
        // Don't intersect hallway
        && ! rectangle.tiles.exists(w.hallway.contains)
        // Don't intersect filler
        && ! placements.exists(_.tileDistanceChebyshev(tile) < 2)
        // Don't intersect original placements
        // Did we obviate this by introducing canPlace() ?
        && ! w.buildings.exists(b =>
          64 +  zone.heart.center.pixelDistanceChebyshev(b._1.topLeftPixel.add(16 * b._2.tileWidthPlusAddon, 16 * b._2.tileHeight))
          <     zone.heart.center.pixelDistanceChebyshev(tile.topLeftPixel.add(16 * 2,                       16 * 2)))
        && ! w.buildings.exists(b => Maff.rectanglesIntersect(
          b._1.x, b._1.y, b._1.x + b._2.tileWidthPlusAddon, b._1.y + b._2.tileHeight,
          tile.x, tile.y, tile.x + 2, tile.y + 2))) {
        placements += tile
      }
    }
  }
}
