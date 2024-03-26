package Placement.Walls

import Information.Geography.Pathfinding.PathfindProfile
import Information.Geography.Types.{Edge, Zone}
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Point, Tile, TileRectangle}
import Mathematics.Shapes.{Pylons, Rectangle, RoundedBox}
import Placement.Generation.TileGeneratorRectangularSweep
import Placement.Walls.WallFillers.{PylonsCannons, WallFiller}
import Placement.Walls.WallProblems.{FailedRecursively, GapTooWide, InsufficientFiller, InsufficientlyTight, IntersectsHall, IntersectsMining, IntersectsPrevious, NoHallway, UnbuildableGranular, UnbuildableTerrain, UnpoweredByFiller, UnpoweredByWall, WallProblem}
import Placement.Walls.WallSpans.{TerrainGas, TerrainHall}
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClasses.UnitClass
import Utilities.?

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class WallFinder(zone: Zone, exit: Edge, entrance: Tile, constraints: Seq[WallConstraint], filler: WallFiller) {
  val metrics           : WallMetrics       = new WallMetrics
  val wallsUnacceptable : ArrayBuffer[Wall] = new ArrayBuffer[Wall]()
  val wallsAcceptable   : ArrayBuffer[Wall] = new ArrayBuffer[Wall]()
  val wallInProgress    : Wall              = new Wall
  var wall              : Option[Wall]      = None

  def generate(): Option[Wall] = {
    placeRequiredBuildings()
    if (wall.isEmpty) return None
    placeFiller()
    wall
  }

  def fail(failedWall: Wall, problem: WallProblem): Boolean = {
    if (With.configuration.debugging) {
      val copy = new Wall(failedWall)
      copy.problems.add(problem)
      wallsUnacceptable += copy
      if (wall.contains(failedWall)) {
        failedWall.problems.add(problem)
        wall = None
      }
    }
    false
  }

  def placeRequiredBuildings(): Option[Wall] = {
    if (constraints.isEmpty)        return None
    if (zone.exitOriginal.isEmpty)  return None
    var constraintIndex : Int   = 0
    var gapsLeft        : Int   = 0

    def constraint: WallConstraint = wallInProgress.constraint
    def pickNextConstraint(): Unit = {
      wallInProgress.buildings.clear()
      wallInProgress.constraint = constraints(constraintIndex)
      wallInProgress.gap        = None
      wallInProgress.hallway    = Seq.empty
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
          return fail(wallInProgress, GapTooWide)
        }

        /* Disabling because we're changing the way we iterate to avoid having to do this check, at a cost of rejecting walls where the gap is at the far end
        // Use Chebyshev distance to check gap size because cater-corner diagonals are not acceptable
        if (horizon.exists(t => boundaryEnd.exists(_.tileDistanceChebyshev(t) <= gapsLeft))) {
          metrics.gapTooNarrow += 1
          return false
        }
        */
        // If the wall has a Pylon, then it must power the other buildings.
        val pylons = wallInProgress.buildings.filter(_._2.powers)
        if (pylons.nonEmpty) {
          val unpowered = wallInProgress.buildings.find(b => b._2.requiresPsi && ! pylons.exists(p => Pylons.powers(p._1, b._1, b._2)))
          if (unpowered.isDefined) {
            return fail(wallInProgress, UnpoweredByWall)
          }
        }

        if (wallInProgress.gap.isEmpty && gapsLeft > 0) {
          val gapCandidates       = horizon.flatMap(_.adjacent4).toSet.filter(t => t.walkable && ! wallInProgress.buildings.exists(b => b._2.tileAreaPlusAddon.add(b._1).contains(t)))
          wallInProgress.gap      = Maff.minBy(gapCandidates)(t => boundaryEnd.map(_.tileDistanceChebyshev(t)).min)
          if (wallInProgress.gap.forall(t => boundaryEnd.map(_.tileDistanceChebyshev(t)).min > gapsLeft)) {
            return fail(wallInProgress, GapTooWide)
          }
        }

        wallsAcceptable += new Wall(wallInProgress)
        wallInProgress.score = scoreWall(wallInProgress)
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
          (0 to gapsLeft).exists(gapsUsed => {
            val gapOffset =
                    if (relative.x == 0)          Point(- gapsUsed,   0)
              else  if (relative.y == 0)          Point(  0,        - gapsUsed)
              else  if (relative.x == width + 1)  Point(  gapsUsed,   0)
              else                                Point(  0,          gapsUsed)
            val buildingTile = horizonTile
              .subtract(width, height)
              .add(relative)
              .add(gapOffset)
            lazy val (alleyLeft, alleyRight, alleyUp, alleyDown) = wallInProgress.buildings.lastOption.map(b => {
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

            // Gaps must meet tightness criterion
            else if (gapsUsed == 0
              && (constraint.blocksUnit.width < 32 || constraint.blocksUnit.height < 32) // Don't even bother checking for big units
              && (alleyLeft >= constraint.blocksUnit.width || alleyRight >= constraint.blocksUnit.width || alleyUp >= constraint.blocksUnit.height || alleyDown >= constraint.blocksUnit.height)) {
              return fail(wallInProgress, InsufficientlyTight)
            }

            // Success! Place the next building.
            else {
              // Apply changes
              wallInProgress.buildings += ((buildingTile, building))

              // Preserve state for later reversion
              val gapBefore = wallInProgress.gap
              if (gapBefore.isEmpty && gapsUsed > 0) {
                wallInProgress.gap = Some(horizonTile.add(gapOffset.direction.x, gapOffset.direction.y))
              }
              // Test changes
              val output = tryPlace(building.tileAreaPlusAddon.tilesAtEdge.map(buildingTile.add), buildingsLeft.drop(1), gapsLeft - gapsUsed)
              if ( ! output) {
                fail(wallInProgress, FailedRecursively)
              }
              // Revert changes
              wallInProgress.gap = gapBefore
              wallInProgress.buildings.remove(wallInProgress.buildings.length - 1)
              output
            }}))})
    }

    while (constraintIndex < constraints.length && wall.isEmpty) {
      pickNextConstraint()
      val permutations = constraint.buildings.permutations.toVector.distinct
      metrics.permutations += permutations.length
      permutations.foreach(tryPlace(zone.wallPerimeterEntrance, _, constraint.gapTiles))
      wall = Maff.minBy(wallsAcceptable)(_.score)
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
      return fail(wallInProgress, UnbuildableTerrain)
    }

    // Buildings can't intersect
    if (wallInProgress.buildings.exists(existingBuilding =>
      Maff.rectanglesIntersect(
        buildingTile.x,
        buildingTile.y,
        buildingEndX,
        buildingEndY,
        existingBuilding._1.x,
        existingBuilding._1.y,
        existingBuilding._1.x + existingBuilding._2.tileWidthPlusAddon,
        existingBuilding._1.y + existingBuilding._2.tileHeight))) {
      return fail(wallInProgress, IntersectsPrevious)
    }

    // Buildings can't block town hall
    if (buildingTile.zone.bases.exists(b => Maff.rectanglesIntersect(buildingTile.x, buildingTile.y, buildingEndX, buildingEndY,
      b.townHallArea.startInclusive.x, b.townHallArea.startInclusive.y,
      b.townHallArea.endExclusive.x, b.townHallArea.endExclusive.y))) {
      return fail(wallInProgress, IntersectsHall)
    }

    // Buildings must not block mining
    if(buildingTiles.exists(t => t.base.exists(_.resourcePathTiles.contains(t)))) {
      return fail(wallInProgress, IntersectsMining)
    }

    // Tiles must be buildable (eg. not blocked by neutrals)
    if (buildingTiles.exists( ! _.buildable)) {
      return fail(wallInProgress, UnbuildableGranular)
    }

    true
  }

  def scoreWall(wall: Wall): Double = {
    if (wall.buildings.isEmpty) return 0.0
    val rectangles = wall.buildings.map(b => new TileRectangle(b._1, b._2.tileWidthPlusAddon, b._2.tileHeight))
    val expanded  = rectangles.map(_.expand(1, 1))
    val union     = expanded.reduce(_.add(_))
    val perimeter = union.tiles.count(t => ! rectangles.exists(_.contains(t)) && expanded.exists(_.contains(t)))
    perimeter
  }

  def scoreWallByArea(wall: Wall): Double = {
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
      val hallwayWalls    = (wall.get.buildings.flatMap(p => p._2.tileAreaPlusAddon.add(p._1).tiles) ++ zone.bases.flatMap(_.townHallArea.tiles)).toSet
      val hallwayProfile  = new PathfindProfile(gap, Some(entrance), employGroundDist = true, alsoUnwalkable = hallwayWalls, allowDiagonals = false)
      val hallwayPath     = hallwayProfile.find.tiles
      wall.get.hallway    = hallwayPath.getOrElse(Seq.empty)
      // We can't safely fill without preserving a path
      if (wall.get.hallway.isEmpty) {
        fail(wall.get, NoHallway)
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
        fail(wall.get, InsufficientFiller)
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
          fail(wall.get, UnpoweredByFiller)
          return
        }
        val candidates  = scores.filter(_._3 == maxScore)
        val best        = candidates.maxBy(_._1.topLeftPixel.add(32, 32).pixelDistanceSquared(centroid))
        wall.get.buildings -= ((best._1, best._2))
        wall.get.buildings += ((best._1, Protoss.Pylon))
        if (unpowered.exists(_._2.tileWidth > 2)) {
          fail(wall.get, UnpoweredByFiller)
          return
        }
      }
    }
  }

  def fill2x2Offset(offset: Point, placements: ArrayBuffer[Tile]): Unit = {
    val w = wall.get
    val buildingCentroid  = Maff.centroid(w.buildings.map(b => b._1.topLeftPixel.add(16 * b._2.tileWidthPlusAddon, 16 * b._2.tileHeight))).walkablePixel
    val buildingTiles     = w.buildings.flatMap(b => b._2.tileAreaPlusAddon.add(b._1).expand(4, 4).tiles)
    val tiles             = zone.tiles ++ buildingTiles
    val generatorBounds   = new TileRectangle(tiles)
    val startOrigin       = zone.metro.flatMap(_.main.map(_.heart)).getOrElse(With.geography.startLocations.minBy(_.groundPixels(zone.heart)))
    val startRemote       = With.geography.startLocations.maxBy(_.groundPixels(startOrigin))
    val generatorStart    = tiles.filter(_.buildable).maxBy(_.groundPixels(startOrigin))
    val generatorEnd      = tiles.filter(_.buildable).minBy(_.groundPixels(startOrigin))
    val direction         = generatorStart.groundDirectionTo(generatorEnd)
    val generator         = new TileGeneratorRectangularSweep(generatorStart, generatorBounds.startInclusive, generatorBounds.endExclusive, direction)
    def tryFill(tile: Tile): Unit = {
      lazy val rectangle    = new TileRectangle(tile, 2, 2)
      lazy val fillerCenter = tile.topLeftPixel.add(16 * 2, 16 * 2)
      if (tile.zone == zone
        // Don't put filler outside the wall
        && fillerCenter.groundPixels(startRemote) >= buildingCentroid.groundPixels(startRemote)
        // Is legal placement
        && canPlace(2, 2, tile, countMetrics = false)
        // Don't intersect hallway
        && ! rectangle.tiles.exists(w.hallway.contains)
        // Don't intersect filler
        && ! placements.exists(_.tileDistanceChebyshev(tile) < 2)

        // Don't intersect original placements
        // Did we obviate this by introducing canPlace() ?
        && ! w.buildings.exists(b => Maff.rectanglesIntersect(
          b._1.x,
          b._1.y,
          b._1.x + b._2.tileWidthPlusAddon,
          b._1.y + b._2.tileHeight,
          tile.x,
          tile.y,
          tile.x + 2,
          tile.y + 2))) {
        placements += tile
      }
    }
    while (generator.hasNext && placements.length < 16) { tryFill(generator.next()) }

    // The generator can miss spots when the wall is near or outside the zone boundary
  }

  override def toString: String = f"WallFinder ${zone.name} ${zone.heart} $wall"
}
