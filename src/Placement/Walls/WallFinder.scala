package Placement.Walls

import Information.Geography.Pathfinding.PathfindProfile
import Information.Geography.Types.{Edge, Zone}
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Point, Tile}
import Mathematics.Shapes.{Corners, Pylons, Rectangle, RoundedBox}
import Placement.Generation.TileGeneratorRectangularSweep
import Placement.Walls.WallSpans.{TerrainGas, TerrainHall}
import ProxyBwapi.Races.{Neutral, Protoss}
import ProxyBwapi.UnitClasses.UnitClass

import scala.collection.mutable.ArrayBuffer

class WallFinder(zone: Zone, exit: Edge, entrance: Tile, constraints: Seq[WallConstraint], filler: Seq[UnitClass]) {
  val metrics = new WallMetrics
  val wallScores = new ArrayBuffer[(Wall, Double)]()
  var wall: Option[Wall] = None

  def generate(): Option[Wall] = {
    placeRequiredBuildings()
    if (wall.isEmpty) return None
    placeFiller()
    wall
  }

  def placeRequiredBuildings(): Option[Wall] = {
    if (constraints.isEmpty)        return None
    if (zone.exitOriginal.isEmpty)  return None

    val incompleteWall  : Wall  = new Wall
    var constraintIndex : Int   = 0
    var gapsLeft        : Int   = 0

    def constraint: WallConstraint = incompleteWall.constraint
    def pickNextConstraint(): Unit = {
      incompleteWall.buildings.clear()
      incompleteWall.constraint = constraints(constraintIndex)
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

      // We've placed all the buildings.
      // Now check if the entire configuration is valid.
      if (buildingsLeft.isEmpty) {
        // TODO: If gapsLeft is zero enforce constraint.blocksUnit

        //
        if (horizon.forall(t => boundaryEnd.forall(_.tileDistanceManhattan(t) > 1 + gapsLeft))) {
          metrics.gapTooWide += 1
          return false
        }

        // Use Chebyshev distance to check gap size because cater-corner diagonals are not acceptable
        if (horizon.exists(t => boundaryEnd.exists(_.tileDistanceChebyshev(t) <= gapsLeft))) {
          metrics.gapTooNarrow += 1
          return false
        }

        // If the wall has a Pylon, then all buildings need to be powered.
        val pylons = incompleteWall.buildings.filter(_._2 == Protoss.Pylon)
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
      val building  = buildingsLeft.head
      val width     = building.tileWidthPlusAddon
      val height    = building.tileHeight

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
            val buildingEndX = buildingTile.x + width
            val buildingEndY = buildingTile.y + height
            lazy val (alleyLeft, alleyRight, alleyUp, alleyDown) = incompleteWall.buildings.lastOption.map(b => {
              val lastBuilding  = b._2
              val lastTile      = b._1
              val tileGapLeft   = buildingTile.x  - lastTile.x      - lastBuilding.tileWidthPlusAddon
              val tileGapRight  = lastTile.x      - buildingTile.x  - width
              val tileGapUp     = buildingTile.y  - lastTile.y      - lastBuilding.tileHeight
              val tileGapDown   = lastTile.y      - buildingTile.y  - height
              val alleyLeft     = if (tileGapLeft   == 0) lastBuilding.marginRight  + building.marginLeft   else -1
              val alleyRight    = if (tileGapRight  == 0) lastBuilding.marginLeft   + building.marginRight  else -1
              val alleyUp       = if (tileGapUp     == 0) lastBuilding.marginDown   + building.marginUp     else -1
              val alleyDown     = if (tileGapDown   == 0) lastBuilding.marginUp     + building.marginDown   else -1
              (alleyLeft, alleyRight, alleyUp, alleyDown)
            }).getOrElse((-1, -1, -1, -1))

            // Terrain must be buildable
            if ( ! With.grids.buildableW(width)(buildingTile)) {
              metrics.unbuildableTerrain += 1
              false

            // Buildings can't intersect
            } else if (incompleteWall.buildings.exists(existingBuilding => Maff.rectanglesIntersect(buildingTile.x, buildingTile.y, buildingEndX, buildingEndY,
              existingBuilding._1.x, existingBuilding._1.y,
              existingBuilding._1.x + existingBuilding._2.tileWidthPlusAddon, existingBuilding._1.y + existingBuilding._2.tileHeight))) {
              metrics.intersectsPrevious += 1
              false

            // Buildings can't block town hall
            } else if (buildingTile.zone.bases.exists(b => Maff.rectanglesIntersect(buildingTile.x, buildingTile.y, buildingEndX, buildingEndY,
              b.townHallArea.startInclusive.x, b.townHallArea.startInclusive.y,
              b.townHallArea.endExclusive.x, b.townHallArea.endExclusive.y))) {
              metrics.intersectsHall += 1
              false

            // Tiles must be buildable (eg. not blocked by neutrals)
            } else if (Rectangle(width, height).map(buildingTile.add).exists( ! _.buildable)) {
              metrics.unbuildableGranular += 1
              false

            // Buildings must touch the target zone
            } else if (incompleteWall.buildings.isEmpty && Corners(width, height).map(buildingTile.add).forall(_.zone != zone)) {
              metrics.wrongZone += 1
              false

            // Gaps must meet tightness criterion
            } else if (gapsUsed == 0 && (alleyLeft >= constraint.blocksUnit.width || alleyRight >= constraint.blocksUnit.width || alleyUp >= constraint.blocksUnit.height || alleyDown >= constraint.blocksUnit.height)) {
              metrics.insufficientlyTight += 1
              false

            // Success! Place the next building.
            } else {
              // Apply changes
              val gapBefore = incompleteWall.gap
              incompleteWall.buildings += ((buildingTile, building))
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
            }
          }))})
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

  def scoreWall(wall: Wall): Double = {
    if (wall.buildings.isEmpty) return 0.0
    val endTiles  = wall.buildings.map(p => p._1.add(p._2.tileWidth, p._2.tileHeight))
    val xMin      = wall.buildings.map(_._1.x).min
    val yMin      = wall.buildings.map(_._1.y).min
    val xMax      = endTiles.map(_.x).max
    val yMax      = endTiles.map(_.y).max
    val scoreArea = Maff.broodWarDistance(xMin, yMin, xMax, yMax)
    scoreArea
  }

  def placeFiller(): Unit = {
    if (filler.isEmpty) return
    if (wall.get.gap.isDefined) {
      val gap = wall.get.gap.get
      val hallwayWalls      = wall.get.buildings.flatMap(p => p._2.tileArea.add(p._1).tiles).toSet
      val hallwayProfile    = new PathfindProfile(gap, Some(entrance), employGroundDist = true, alsoUnwalkable = hallwayWalls)
      val hallwayPath       = hallwayProfile.find.tiles
      wall.get.hallway      = hallwayPath.getOrElse(Seq.empty)
      if (wall.get.hallway.isEmpty) return // We can't safely fill without preserving a path
    }
    var i = 0
    var lastFiller = Neutral.PsiDisruptor
    var lastFilled = true
    while (i < filler.length) {
      val fill = filler(i)
      // Don't try and fail to fill the same thing multiple times
      if (lastFilled || fill != lastFiller) {
        lastFilled = tryFill(fill)
      }
      lastFiller = fill
      i += 1
    }
  }

  def tryFill(fill: UnitClass): Boolean = {
    val w = wall.get
    val generator = new TileGeneratorRectangularSweep(exit.pixelCenter.tile, zone.boundary.startInclusive, zone.boundary.endExclusive, exit.pixelCenter.directionTo(zone.heart.center))
    while (generator.hasNext) {
      val tile = generator.next()
      if (tile.zone == zone
        && With.grids.buildableW(fill.tileWidthPlusAddon)(tile)
        && ! w.hallway.contains(tile)
        && ! w.buildings.exists(b =>
          64 +  zone.heart.center.pixelDistanceChebyshev(b._1.topLeftPixel.add(16 * b._2.tileWidthPlusAddon, 16 * b._2.tileHeight))
          <     zone.heart.center.pixelDistanceChebyshev(tile.topLeftPixel.add(16 * fill.tileWidthPlusAddon, 16 * fill.tileHeight)))
        && ! w.buildings.exists(b => Maff.rectanglesIntersect(
          b._1.x, b._1.y, b._1.x + b._2.tileWidthPlusAddon, b._1.y + b._2.tileHeight,
          tile.x, tile.y, tile.x + fill.tileWidthPlusAddon, tile.y + fill.tileHeight))) {
        // TODO: Test power
        w.buildings += ((tile, fill))
        return true
      }
    }
    false
  }
}
