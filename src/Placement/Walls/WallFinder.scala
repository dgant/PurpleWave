package Placement.Walls

import Information.Geography.Types.{Edge, Zone}
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Point, Tile, TileRectangle}
import Mathematics.Shapes.{Pylons, Rectangle, RoundedBox}
import Placement.Generation.TileGeneratorRectangularSweep
import Placement.Walls.WallFillers.{PylonsCannons, WallFiller}
import Placement.Walls.WallProblems.{GapTooNarrow, GapTooWide, InsufficientFiller, InsufficientlyTight, IntersectsHall, IntersectsMining, IntersectsPrevious, NoHallway, UnbuildableGranular, UnbuildableTerrain, UnpoweredByFiller, UnpoweredByWall, WallProblem}
import Placement.Walls.WallSpans.{TerrainGas, TerrainHall}
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClasses.UnitClass
import Utilities.?

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class WallFinder(zone: Zone, exit: Edge, entrance: Tile, constraints: Seq[WallConstraint], filler: WallFiller) {
  private val startedNanos                = System.nanoTime()
  private val searchDeadlineNanos         = startedNanos + 45000000000L
  private val overallDeadlineNanos        = startedNanos + 58000000000L
  private val maxCandidatesPerPermutation = 64
  private val maxCandidatesPerConstraint  = 256
  private val maxFillerFinalists          = 256
  val metrics           : WallMetrics       = new WallMetrics
  val wallsUnacceptable : ArrayBuffer[Wall] = new ArrayBuffer[Wall]()
  val wallsAcceptable   : ArrayBuffer[Wall] = new ArrayBuffer[Wall]()
  val wallInProgress    : Wall              = new Wall
  var wall              : Option[Wall]      = None

  private lazy val startOrigin = zone.metro
    .flatMap(_.main.map(_.heart))
    .getOrElse(With.geography.startLocations.minBy(_.groundPixels(zone.heart)))

  def generate(): Option[Wall] = {
    placeRequiredBuildings()
    selectWallWithFiller()
    wall.foreach(assignOutsideHallway)
    metrics.milliseconds = (System.nanoTime() - startedNanos) / 1000000L
    wall
  }

  def fail(failedWall: Wall, problem: WallProblem): Boolean = {
    if (wall.contains(failedWall)) {
      failedWall.problems.add(problem)
      wall = None
    }
    if (With.configuration.debugging) {
      val copy = new Wall(failedWall)
      copy.problems.add(problem)
      if (wallsUnacceptable.length < 5000) wallsUnacceptable += copy
    }
    false
  }

  def placeRequiredBuildings(): Option[Wall] = {
    if (constraints.isEmpty)        return None
    if (zone.exitOriginal.isEmpty)  return None
    var constraintIndex : Int   = 0
    var gapsLeft        : Int   = 0
    var candidatesThisConstraint  = 0
    var candidatesThisPermutation = 0

    def searchExpired: Boolean = System.nanoTime() >= searchDeadlineNanos

    def constraint: WallConstraint = wallInProgress.constraint
    def pickNextConstraint(): Unit = {
      wallInProgress.buildings.clear()
      wallInProgress.constraint = constraints(constraintIndex)
      wallInProgress.gap        = None
      wallInProgress.hallway    = Seq.empty
      constraintIndex += 1
      gapsLeft = constraint.gapTiles
      candidatesThisConstraint = 0
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

        // A gap recorded while placing a building must not be covered by that building.
        if (wallInProgress.gap.exists(gap => wallInProgress.buildings.exists(b => b._2.tileAreaPlusAddon.add(b._1).contains(gap)))) {
          return fail(wallInProgress, GapTooNarrow)
        }

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

        wallInProgress.score = scoreWall(wallInProgress)
        wallsAcceptable += new Wall(wallInProgress)
        candidatesThisConstraint += 1
        candidatesThisPermutation += 1
        metrics.candidatesGenerated += 1
        return searchExpired ||
          candidatesThisConstraint >= maxCandidatesPerConstraint ||
          candidatesThisPermutation >= maxCandidatesPerPermutation
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
              // Revert changes
              wallInProgress.gap = gapBefore
              wallInProgress.buildings.remove(wallInProgress.buildings.length - 1)
              output
            }}))})
    }

    while (constraintIndex < constraints.length && ! searchExpired) {
      pickNextConstraint()
      val permutations = constraint.buildings.permutations.toVector.distinct
      metrics.permutations += permutations.length
      val startingBoundary = zone.wallPerimeterEntrance
      permutations.iterator.takeWhile(_ => ! searchExpired && candidatesThisConstraint < maxCandidatesPerConstraint).foreach(buildings => {
        candidatesThisPermutation = 0
        tryPlace(startingBoundary, buildings, constraint.gapTiles)
      })
    }
    Maff.minBy(wallsAcceptable)(_.score)
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
    if (buildingTiles.exists(t => ! t.buildable || (! With.self.isZerg && t.creep))) {
      return fail(wallInProgress, UnbuildableGranular)
    }

    true
  }

  private lazy val townHallTiles = zone.bases.flatMap(_.townHallArea.tiles)
  def scoreWall(wall: Wall): Double = {
    if (wall.buildings.isEmpty) return 0.0
    val rectangles = wall.buildings.map(b => new TileRectangle(b._1, b._2.tileWidthPlusAddon, b._2.tileHeight))
    val compactRectangles = rectangles ++ wall.gap.map(new TileRectangle(_, 1, 1))
    val expanded  = compactRectangles.map(_.expand(1, 1))
    val union     = expanded.reduce(_.add(_))
    val perimeter = union.tiles.count(t => ! compactRectangles.exists(_.contains(t)) && expanded.exists(_.contains(t)))
    val distance  = union.center.walkableTile.groundTiles(zone.heart)
    val margin    = Maff.min(townHallTiles.flatMap(hallTile => rectangles.flatMap(_.tiles).map(_.tileDistanceChebyshev(hallTile)))).getOrElse(0)
    val centersX2 = wall.buildings.map(b => 2 * b._1.x + b._2.tileWidthPlusAddon)
    val centersY2 = wall.buildings.map(b => 2 * b._1.y + b._2.tileHeight)
    val alignment = Math.min(centersX2.max - centersX2.min, centersY2.max - centersY2.min) / 2.0
    val horizontal = centersX2.max - centersX2.min >= centersY2.max - centersY2.min
    val buildingCenterX2 = centersX2.sum / wall.buildings.length
    val buildingCenterY2 = centersY2.sum / wall.buildings.length
    val insideEdges = wall.buildings.map(b =>
      if (horizontal) {
        if (2 * startOrigin.y < buildingCenterY2) b._1.y else b._1.y + b._2.tileHeight
      } else {
        if (2 * startOrigin.x < buildingCenterX2) b._1.x else b._1.x + b._2.tileWidthPlusAddon
      })
    val insideEdgeAlignment = insideEdges.max - insideEdges.min
    val gapPositionPenalty = wall.gap.map(gap => {
      val adjacentBuildings = rectangles.count(rectangle => gap.adjacent4.exists(rectangle.contains))
      if (adjacentBuildings >= 2) {
        0
      } else if (gap.groundPixels(startOrigin) >= union.center.walkableTile.groundPixels(startOrigin)) {
        400
      } else {
        1200
      }
    }).getOrElse(0)
    (10 * perimeter
    + 12 * alignment
    + 24 * insideEdgeAlignment
    + 60 * Math.max(0, 6 - margin)
    + gapPositionPenalty
    - 6 * distance)

  }

  private def requiredBuildings(wall: Wall): Seq[(Tile, UnitClass)] = wall.buildings.take(wall.constraint.buildings.length)

  private def distanceFromRequired(wall: Wall, tile: Tile): Int = requiredBuildings(wall).map(required => {
    val xBefore = Math.max(0, required._1.x - tile.x - 2)
    val xAfter  = Math.max(0, tile.x - required._1.x - required._2.tileWidthPlusAddon)
    val yBefore = Math.max(0, required._1.y - tile.y - 2)
    val yAfter  = Math.max(0, tile.y - required._1.y - required._2.tileHeight)
    Math.max(Math.max(xBefore, xAfter), Math.max(yBefore, yAfter))
  }).min

  private def scoreFillerGeometry(wall: Wall, tiles: Seq[Tile]): Double = {
    val required = requiredBuildings(wall)
    if (required.isEmpty) return 0.0
    val centersX2 = required.map(b => 2 * b._1.x + b._2.tileWidthPlusAddon)
    val centersY2 = required.map(b => 2 * b._1.y + b._2.tileHeight)
    val horizontal = centersX2.max - centersX2.min >= centersY2.max - centersY2.min
    val closest = tiles.sortBy(distanceFromRequired(wall, _)).take(6)
    val aligned = closest.groupBy(t => if (horizontal) t.y else t.x).values.map(_.length).reduceOption(Math.max).getOrElse(0)
    val slots = if (closest.isEmpty) 0 else {
      val width  = (closest.map(_.x).max - closest.map(_.x).min) / 2 + 1
      val height = (closest.map(_.y).max - closest.map(_.y).min) / 2 + 1
      width * height
    }
    val missing = 6 - closest.length
    val holes = Math.max(0, slots - closest.length)
    (8 * closest.map(t => Math.min(2, distanceFromRequired(wall, t))).sum
    + 30 * missing
    + 10 * (closest.length - aligned)
    + 6 * holes
    - 20 * Math.min(tiles.length, 8))
  }

  private def scoreFiller(wall: Wall): Double = {
    val requiredCount = wall.constraint.buildings.length
    val fillerBuildings = wall.buildings.drop(requiredCount)
    val cannonTiles = wall.buildings.filter(_._2 == Protoss.PhotonCannon).map(_._1)
    val fillerPylons = fillerBuildings.count(_._2 == Protoss.Pylon)
    scoreFillerGeometry(wall, cannonTiles) + 5 * fillerPylons
  }

  private def selectWallWithFiller(): Unit = {
    wall = None
    wallsAcceptable
      .groupBy(candidate => constraints.indexOf(candidate.constraint))
      .toSeq
      .sortBy(_._1)
      .iterator
      .takeWhile(_ => wall.isEmpty)
      .foreach(group => {
        val filled = new ArrayBuffer[Wall]()
        group._2.sortBy(_.score).foreach(required => if (
          filled.length < maxFillerFinalists &&
          (filled.isEmpty || System.nanoTime() < overallDeadlineNanos)) {
          val candidate = new Wall(required)
          wall = Some(candidate)
          metrics.finalistsEvaluated += 1
          placeFiller()
          if (wall.contains(candidate)) {
            if (filler == PylonsCannons) candidate.score += scoreFiller(candidate)
            filled += candidate
          }
        })
        wall = Maff.minBy(filled)(_.score)
      })
    metrics.walkwayLength = wall.map(_.hallway.length).getOrElse(0)
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
      val hallwayPath     = findHallway(gap, entrance, hallwayWalls)
      wall.get.hallway    = hallwayPath.getOrElse(Seq.empty)
      // We can't safely fill without preserving a path
      if (wall.get.hallway.isEmpty) {
        fail(wall.get, NoHallway)
        return
      }
    }

    if (filler == PylonsCannons) fill2X2()
    if (wall.nonEmpty) validateWalkway()
  }

  private def validateWalkway(): Unit = {
    val w = wall.get
    val walls = (w.buildings.flatMap(p => p._2.tileAreaPlusAddon.add(p._1).tiles) ++ zone.bases.flatMap(_.townHallArea.tiles)).toSet
    val path = w.gap.flatMap(gap => findHallway(gap, entrance, walls))
    if (path.isEmpty) {
      fail(w, NoHallway)
    } else {
      w.hallway = path.get
    }
  }

  private def assignOutsideHallway(w: Wall): Unit = {
    val walls = (w.buildings.flatMap(p => p._2.tileAreaPlusAddon.add(p._1).tiles) ++ zone.bases.flatMap(_.townHallArea.tiles)).toSet
    w.hallwayOutside = w.gap.toSeq.flatMap(gap => {
      val insideDirection = w.hallway.find(_ != gap).map(_.directionTo(gap))
      val outsideDirection = insideDirection.getOrElse(gap.directionTo(exit.otherSideof(zone).heart))
      (0 to 4)
        .map(distance => gap.add(outsideDirection.x * distance, outsideDirection.y * distance))
        .takeWhile(tile => tile.walkableUnchecked && ! walls.contains(tile))
    })
  }

  def fill2X2(): Unit = {
    val inside = findInsideTiles(wall.get)
    val offsets = Vector(Point(0, 0), Point(0, 1), Point(1, 0), Point(1, 1))
    val placements = new mutable.HashMap[Point, ArrayBuffer[Tile]]
    offsets.foreach(offset => {
      placements(offset) = new ArrayBuffer[Tile]()
      fill2x2Offset(offset, placements(offset), inside)
    })
    val tiles = placements.values.minBy(scoreFillerGeometry(wall.get, _)).take(8)
    if (filler == PylonsCannons) {
      val requiredPylons = wall.get.buildings.filter(_._2 == Protoss.Pylon).map(_._1)
      val maxFillerPylons = 2 - requiredPylons.length
      val configurations = (0 to Math.max(0, maxFillerPylons)).flatMap(pylonCount => tiles.combinations(pylonCount).flatMap(fillerPylons => {
        val pylons = requiredPylons ++ fillerPylons
        val requiredPowered = wall.get.buildings.filter(_._2.requiresPsi).forall(b => pylons.exists(p => Pylons.powers(p, b._1, b._2)))
        val poweredCannons = tiles.filterNot(fillerPylons.contains).filter(c => pylons.exists(p => Pylons.powers(p, c, Protoss.PhotonCannon)))
        Some((fillerPylons, poweredCannons)).filter(_ => requiredPowered && poweredCannons.length >= 6)
      }))
      if (configurations.isEmpty) {
        fail(wall.get, InsufficientFiller)
        return
      }
      val best = configurations.minBy(c => (c._1.length, scoreFillerGeometry(wall.get, c._2)))
      wall.get.buildings ++= best._2.map(t => (t, Protoss.PhotonCannon))
      wall.get.buildings ++= best._1.map(t => (t, Protoss.Pylon))
    }
  }

  private case class HallwayState(tile: Tile, direction: Int, steps: Int, turns: Int)

  private val hallwayDirections = Vector(Point(0, -1), Point(1, 0), Point(0, 1), Point(-1, 0))

  private def findHallway(start: Tile, endRaw: Tile, walls: Set[Tile]): Option[Seq[Tile]] = {
    val end = endRaw.walkableTile
    val deltaX = end.x - start.x
    val deltaY = end.y - start.y
    val preferredDirection = if (Math.abs(deltaX) >= Math.abs(deltaY)) {
      if (deltaX >= 0) 1 else 3
    } else {
      if (deltaY >= 0) 2 else 0
    }
    implicit val ordering: Ordering[HallwayState] = Ordering.by(state => (- state.steps, - state.turns))
    val horizon = mutable.PriorityQueue.empty[HallwayState]
    val best = new mutable.HashMap[(Int, Int), (Int, Int)]
    val previous = new mutable.HashMap[(Int, Int), (Int, Int)]
    val startKey = (start.i, preferredDirection)
    best(startKey) = (0, 0)
    horizon += HallwayState(start, preferredDirection, 0, 0)
    while (horizon.nonEmpty) {
      val current = horizon.dequeue()
      val currentKey = (current.tile.i, current.direction)
      if (best.get(currentKey).contains((current.steps, current.turns))) {
        if (current.tile == end) {
          val output = new ArrayBuffer[Tile]
          var key = currentKey
          output += new Tile(key._1)
          while (previous.contains(key)) {
            key = previous(key)
            output += new Tile(key._1)
          }
          return Some(output.reverse)
        }
        hallwayDirections.indices.foreach(direction => {
          val offset = hallwayDirections(direction)
          val next = current.tile.add(offset.x, offset.y)
          if (next.valid && next.walkableUnchecked && ! walls.contains(next)) {
            val nextSteps = current.steps + 1
            val nextTurns = current.turns + Maff.fromBoolean(direction != current.direction)
            val nextKey = (next.i, direction)
            val nextCost = (nextSteps, nextTurns)
            if (best.get(nextKey).forall(cost => nextSteps < cost._1 || (nextSteps == cost._1 && nextTurns < cost._2))) {
              best(nextKey) = nextCost
              previous(nextKey) = currentKey
              horizon += HallwayState(next, direction, nextSteps, nextTurns)
            }
          }
        })
      }
    }
    None
  }

  private def findInsideTiles(w: Wall): Set[Tile] = {
    val blockers = (w.buildings.flatMap(p => p._2.tileAreaPlusAddon.add(p._1).tiles)
      ++ w.gap
      ++ zone.bases.flatMap(_.townHallArea.tiles)).toSet
    val available = zone.tiles.filter(tile => tile.walkableUnchecked && ! blockers.contains(tile)).toSet
    if (available.isEmpty) return Set.empty
    val seed = available.minBy(_.groundPixels(entrance))
    val output = new mutable.HashSet[Tile]
    val horizon = mutable.Queue(seed)
    output += seed
    while (horizon.nonEmpty) {
      horizon.dequeue().adjacent4.foreach(next => if (available.contains(next) && ! output.contains(next)) {
        output += next
        horizon.enqueue(next)
      })
    }
    output.toSet
  }

  def fill2x2Offset(offset: Point, placements: ArrayBuffer[Tile], inside: Set[Tile]): Unit = {
    val w = wall.get
    val buildingCentroid  = Maff.centroid(w.buildings.map(b => b._1.topLeftPixel.add(16 * b._2.tileWidthPlusAddon, 16 * b._2.tileHeight))).walkablePixel
    val buildingTiles     = w.buildings.flatMap(b => b._2.tileAreaPlusAddon.add(b._1).expand(3, 3).tiles)
    val startRemote       = With.geography.startLocations.maxBy(_.groundPixels(startOrigin))
    val tiles             = (zone.tiles ++ buildingTiles).filter(t => t.buildable && t.groundTiles(startOrigin) < 256)
    val generatorBounds   = new TileRectangle(tiles)
    val generatorStart    = tiles.minBy(_.groundPixels(startRemote))
    val generatorEnd      = tiles.minBy(_.groundPixels(startOrigin))
    val direction         = generatorStart.groundDirectionTo(generatorEnd)
    val generator         = new TileGeneratorRectangularSweep(generatorStart, generatorBounds.startInclusive, generatorBounds.endExclusive, direction)
    def tryFill(tile: Tile): Unit = {
      lazy val rectangle    = new TileRectangle(tile, 2, 2)
      lazy val fillerCenter = tile.topLeftPixel.add(16 * 2, 16 * 2)
      if ((tile.x & 1) == offset.x
        && (tile.y & 1) == offset.y
        && tile.zone == zone
        // Is legal placement
        && canPlace(2, 2, tile, countMetrics = false)
        // Keep filler on the base side of the wall
        && rectangle.tiles.forall(inside.contains)
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
    while (generator.hasNext) { tryFill(generator.next()) }

    Maff.sortStablyInPlaceBy(placements)(_.groundPixels(buildingCentroid))
  }

  override def toString: String = f"WallFinder ${zone.name} ${zone.heart} $wall"
}
