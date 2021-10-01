package Information.Geography.Pathfinding

import Information.Geography.Pathfinding.Types.TilePath
import Lifecycle.With
import Mathematics.Points.Tile
import Mathematics.Maff

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.language.postfixOps

trait TilePathfinder {

  val stampDefault: Long = Long.MinValue
  var stampCurrent: Long = 0L
  private var tilesExplored: Long = 0
  private val tilesExploredMax: Long = 4 * (With.mapTileWidth + With.mapTileHeight)

  // Performance metrics
  var aStarPathfinds: Long = 0
  var aStarExplorationMaxed: Long = 0
  var aStarOver1ms: Long = 0
  var aStarNanosMax: Long = 0
  var aStarNanosTotal: Long = 0
  var aStarPathLengthMax: Long = 0
  var aStarPathLengthTotal: Long = 0
  var aStarTilesExploredMax: Long = 0
  var aStarTilesExploredTotal: Long = 0

  def profileDistance(start: Tile, end: Tile): PathfindProfile = new PathfindProfile(start, Some(end))
  private var tiles: Array[TileState] = Array.empty
  private val horizon = new mutable.PriorityQueue[TileState]()(Ordering.by(t => -t.totalCostFloor))

  @inline private final def totalRepulsion(profile: PathfindProfile, tile: Tile): Double = {
    var output = 0.0
    var i = 0
    val length = profile.repulsors.length
    while (i < length) {
      val repsulsor = profile.repulsors(i)
      val distance  = repsulsor.source.pixelDistance(tile.center)
      output += repsulsor.magnitude * (1 + repsulsor.rangePixels) / (1 + distance)
      i += 1
    }
    output
  }

  // Best cost from the start tile to this tile.
  // In common A* parlance, this is the gScore.
  val maxMobility = 6.0
  @inline private final def costFromStart(profile: PathfindProfile, toTile: Tile, hypotheticalFrom: Option[Tile] = None): Double = {
    val i = toTile.i
    val toState = tiles(i)
    val fromState = hypotheticalFrom.map(t => tiles(t.i)).orElse(toState.cameFrom.map(t => tiles(t.i)))
    if (fromState.isEmpty) return 0
    val fromTile = fromState.get
    val costSoFar       : Double  = fromState.get.costFromStart
    val costDistance    : Double  = if (fromTile.tile.x == toTile.x || fromTile.tile.y == toTile.y) 1 else Maff.sqrt2
    val costEnemyVision : Double  = if (toTile.visibleToEnemy) profile.costEnemyVision else 0.0
    val costImmobility  : Double  = if (profile.costImmobility == 0) 0.0 else profile.costImmobility * Math.max(0.0, maxMobility - toTile.mobility) / maxMobility
    val costOccupancy   : Double  = if (profile.costOccupancy == 0) 0 else {
      // Intuition: We want to keep this value scaled around 0-1 so we can reason about costOccupancy
      // Signum: Scale-invariant
      // Sigmoid: Tiebreaks equally signed vectors with different scales
      val diff = With.coordinator.gridPathOccupancy.getUnchecked(i) - With.coordinator.gridPathOccupancy.getUnchecked(fromTile.i)
      profile.costOccupancy * 0.5 * (Maff.fastSigmoid(diff) + 0.5 + 0.5 * Maff.signum(diff))
    }
    val costRepulsion: Double = if (profile.costRepulsion == 0 || profile.maxRepulsion == 0) 0 else {
      // Intuition: We want to keep this value scaled around 0-1 so we can reason about costRepulsion
      // and it must be non-negative to preserve heuristic admissibility
      // Signum: Scale-invariant
      // Sigmoid: Tiebreaks equally signed vectors with different scales
      val diff = toState.repulsion - fromState.get.repulsion
      profile.costRepulsion * 0.5 * (Maff.fastSigmoid(diff) + 0.5 + 0.5 * Maff.signum(diff))
    }
    val costThreat: Double  = if (profile.costThreat == 0) 0 else profile.costThreat * Math.max(0, profile.threatGrid.getUnchecked(i) - profile.threatGrid.getUnchecked(fromTile.i)) // Max?
    val output = costSoFar + costDistance + costEnemyVision + costImmobility + costOccupancy + costRepulsion + costThreat
    output
  }

  // The A* admissable heuristic: The lowest possible cost to the end of the path.
  // In common parlance, this is h()
  //
  // Threat-aware pathfinding makes it easy to introduce a non-admissible heuristic,
  // so be careful when modifying this.
  @inline private final def costToEndFloor(profile: PathfindProfile, tile: Tile): Double = {
    val i = tile.i

    // We're using "Depth into enemy range" as our threat cost.
    // It's a good metric for measuring progress towards escaping the enemy, but fails to capture how *much* damage they're threatening at each location
    //
    // To escape a tile that's 5 tiles into enemy range means we have to pass through tiles of value 5+4+3+2+1
    // So the floor of the cost we'll pay is the Gaussian expansion of the threat cost at the current tile.

    val costDistanceToEnd   : Double = profile.end.map(end => if (profile.crossUnwalkable || ! profile.employGroundDist) tile.tileDistanceFast(end) else tile.pixelDistanceGround(end) / 32.0).getOrElse(0.0)
    val costOutOfRepulsion  : Double = profile.costRepulsion * Maff.fastSigmoid(tiles(i).repulsion) // Hacky; used to smartly tiebreak tiles that are otherwise h() = 0. Using this formulation to minimize likelihood of breaking heuristic requirements
    val costOutOfThreat     : Double = profile.costThreat * profile.threatGrid.getUnchecked(i)

    Math.max(costDistanceToEnd, costOutOfThreat)
  }

  private def failure(tile: Tile) = TilePath(tile, tile, Int.MaxValue, None)

  def aStar(from: Tile, to: Tile): TilePath = new PathfindProfile(from, Some(to)).find

  // I don't want to stop
  // until I reach the top.
  // Baby I'm A*. --Prince
  def aStar(profile: PathfindProfile): TilePath = {
    val nanosBefore = System.nanoTime()
    val output = aStarInner(profile)
    val nanosDelta = Math.max(0, System.nanoTime() - nanosBefore)
    val pathLength = output.tiles.map(_.length).getOrElse(0)
    aStarPathfinds += 1
    aStarExplorationMaxed += Maff.fromBoolean(tilesExplored >= tilesExploredMax)
    aStarOver1ms += Maff.fromBoolean(nanosDelta > 1e6)
    aStarNanosMax = Math.max(aStarNanosMax, nanosDelta)
    aStarNanosTotal += nanosDelta
    aStarPathLengthMax = Math.max(aStarPathLengthMax, pathLength)
    aStarPathLengthTotal += pathLength
    aStarTilesExploredMax = Math.max(aStarTilesExploredMax, tilesExplored)
    aStarTilesExploredTotal += tilesExplored
    output
  }
  private def aStarInner(profile: PathfindProfile): TilePath = {
    val startTile = profile.start
    if ( ! startTile.valid) return failure(startTile)

    if (tiles.isEmpty || stampCurrent == Long.MaxValue) {
      tiles = With.geography.allTiles.indices.map(i => new TileState(new Tile(i))).toArray
    }
    if (stampCurrent == Long.MaxValue) { stampCurrent = stampDefault }
    stampCurrent += 1
    profile.updateRepulsion()
    horizon.clear()
    val startTileState = tiles(startTile.i)
    val alsoUnwalkableI = profile.alsoUnwalkable.map(_.i)
    startTileState.setVisited()
    startTileState.setEnqueued()
    startTileState.setCameFrom(startTile)
    startTileState.setRepulsion(totalRepulsion(profile, startTile))
    startTileState.setCostFromStart(costFromStart(profile, startTile))
    startTileState.setTotalCostFloor(costToEndFloor(profile, startTile))
    startTileState.setPathLength(1)
    horizon += startTileState
    tilesExplored = 0

    while (horizon.nonEmpty && tilesExplored < tilesExploredMax) {
      tilesExplored += 1
      val bestTileState = horizon.dequeue()
      val bestTile = bestTileState.tile
      bestTileState.setVisited()

      // Are we done?
      val atProfileEnd = profile.end.exists(end =>
        end.i == bestTileState.i
        || (
          profile.endDistanceMaximum > 0
          && end.tileDistanceFast(bestTile) <= profile.endDistanceMaximum
          && (profile.crossUnwalkable || end.pixelDistanceGround(bestTile) <= 32 * profile.endDistanceMaximum)))
      if (
        profile.lengthMaximum.exists(_ <= Math.round(bestTileState.pathLength)) // Rounding encourages picking diagonal paths for short maximum lengths
        || (tilesExplored == tilesExploredMax && profile.acceptPartialPath)
        || (
          (atProfileEnd || (profile.end.isEmpty && (profile.endUnwalkable || bestTile.walkableUnchecked)))
          && profile.lengthMinimum.forall(_ <= bestTileState.pathLength)
          && profile.threatMaximum.forall(_ >= profile.threatGrid.getUnchecked(bestTile.i)))) {
        val output = TilePath(
          startTile,
          bestTile,
          costFromStart(profile, bestTileState.tile),
          Some(assemblePath(bestTileState)))
        profile.unit.flatMap(_.friendly.map(_.agent)).foreach(_.lastPath = Some(output))
        return output
      }

      val neigborTiles = bestTileState.tile.adjacent8
      var i = 0
      while (i < 8) {
        val neighborTile = neigborTiles(i)
        i += 1
        if (neighborTile.valid) {
          val neighborState = tiles(neighborTile.i)
          val neighborOrthogonal = neighborTile.x == bestTile.x || neighborTile.y == bestTile.y
          // Should we bother visiting this neighbor?
          if (
            ! neighborState.visited

            // Is the neighbor pathable?
            && (profile.crossUnwalkable || (With.grids.walkable.getUnchecked(neighborState.i) && ! alsoUnwalkableI.contains(neighborTile.i)))
            // Can we path from here to the neighbor?
            // (Specifically, if it's a diagonal step, verify that it's achievable)
            && (profile.crossUnwalkable
              || neighborOrthogonal
              || With.grids.walkable.getUnchecked(Tile(neighborTile.x, bestTileState.tile.y).i)
              || With.grids.walkable.getUnchecked(Tile(bestTileState.tile.x, neighborTile.y).i))) {

            val wasEnqueued = neighborState.enqueued
            if ( ! wasEnqueued) {
              neighborState.setRepulsion(totalRepulsion(profile, neighborTile))
            }
            val neighborCostFromStart = costFromStart(profile, neighborTile, Some(bestTile))
            if ( ! wasEnqueued || neighborState.costFromStart > neighborCostFromStart) {
              neighborState.setCameFrom(bestTileState.tile)
              neighborState.setCostFromStart(neighborCostFromStart)
              neighborState.setTotalCostFloor(neighborCostFromStart + costToEndFloor(profile, neighborTile))
              neighborState.setPathLength(bestTileState.pathLength + (if (neighborOrthogonal) 1 else Maff.sqrt2))
            }
            if ( ! wasEnqueued) {
              neighborState.setRepulsion(totalRepulsion(profile, neighborTile))
              neighborState.setEnqueued()
              horizon += neighborState
            }

          }
        }
      }
    }
    failure(startTile)
  }

  private def assemblePath(end: TileState): IndexedSeq[Tile] = {
    val path = new ArrayBuffer[Tile]
    path += end.tile
    var last = end.tile
    while (tiles(last.i).cameFrom.isDefined) {
      last = tiles(last.i).cameFrom.get
      path.append(last)
    }
    path.reverse
  }
}
