package Information.Geography.Pathfinding

import Information.Geography.Pathfinding.Types.TilePath
import Lifecycle.With
import Mathematics.Points.Tile
import Mathematics.PurpleMath

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.language.postfixOps

trait TilePathfinder {

  val stampDefault: Long = Long.MinValue
  var stampCurrent: Long = 0L

  def profileDistance(start: Tile, end: Tile): PathfindProfile = new PathfindProfile(start, Some(end))
  private var tiles: Array[TileState] = Array.empty

  private def startNextSearch() {
    if (tiles.isEmpty || stampCurrent == Long.MaxValue) {
      tiles = With.geography.allTiles.indices.map(i => new TileState(new Tile(i))).toArray
    }
    if (stampCurrent == Long.MaxValue) {
      stampCurrent = stampDefault
    }
    stampCurrent += 1
  }

  @inline private final def totalRepulsion(profile: PathfindProfile, tile: Tile): Double = {
    var output = 0.0
    var i = 0
    val length = profile.repulsors.length
    while (i < length) {
      val repsulsor = profile.repulsors(i)
      val distance  = repsulsor.source.pixelDistance(tile.pixelCenter)
      output += repsulsor.magnitude * (1 + repsulsor.rangePixels) / (1 + distance)
      i += 1
    }
    output
  }

  // Best cost from the start tile to this tile.
  // In common A* parlance, this is the gScore.
  @inline private final def costFromStart(profile: PathfindProfile, toTile: Tile, hypotheticalFrom: Option[Tile] = None): Double = {
    val i = toTile.i
    val toState = tiles(i)
    val fromState = hypotheticalFrom.map(t => tiles(t.i)).orElse(toState.cameFrom.map(t => tiles(t.i)))
    if (fromState.isEmpty) return 0
    val fromTile = fromState.get
    val costSoFar     : Double  = fromState.get.costFromStart
    val costDistance  : Double  = if (fromTile.tile.x == toTile.x || fromTile.tile.y == toTile.y) 1f else PurpleMath.sqrt2f
    val costThreat    : Double  = if (profile.costThreat == 0) 0 else profile.costThreat * Math.max(0, profile.threatGrid.getUnchecked(i) - profile.threatGrid.getUnchecked(fromTile.i)) // Max?
    val costOccupancy : Double  = if (profile.costOccupancy == 0)
      0
    else {
      // Intuition: We want to keep this value scaled around 0-1 so we can reason about costOccupancy
      // Signum: Scale-invariant
      // Sigmoid: Tiebreaks equally signed vectors with different scales
      val diff = With.coordinator.gridPathOccupancy.getUnchecked(i) - With.coordinator.gridPathOccupancy.getUnchecked(fromTile.i)
      profile.costOccupancy * 0.5f * (PurpleMath.fastSigmoid(diff) + 0.5 + 0.5 * PurpleMath.signum(diff))
    }
    val costRepulsion: Double = if (profile.costRepulsion == 0 || profile.maxRepulsion == 0)
      0
    else {
      // Intuition: We want to keep this value scaled around 0-1 so we can reason about costRepulsion
      // and it must be non-negative to preserve heuristic admissibility
      // Signum: Scale-invariant
      // Sigmoid: Tiebreaks equally signed vectors with different scales
      val diff = toState.repulsion - fromState.get.repulsion
      profile.costRepulsion * 0.5f * (PurpleMath.fastSigmoid(diff) + 0.5 + 0.5 * PurpleMath.signum(diff))
    }

    costSoFar + costDistance + costThreat + costOccupancy + costRepulsion
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

    val costDistanceToEnd   : Double = profile.end.map(end => if (profile.crossUnwalkable || ! profile.employGroundDist) tile.tileDistanceFast(end) else tile.groundPixels(end) / 32.0).getOrElse(0.0)
    val costOutOfRepulsion  : Double = profile.costRepulsion * PurpleMath.fastSigmoid(tiles(i).repulsion) // Hacky; used to smartly tiebreak tiles that are otherwise h() = 0. Using this formulation to minimize likelihood of breaking heuristic requirements
    val costOutOfThreat     : Double = profile.costThreat * profile.threatGrid.getUnchecked(i)

    Math.max(costDistanceToEnd, costOutOfThreat)
  }

  private def failure(tile: Tile) = TilePath(tile, tile, Int.MaxValue, None)

  // I don't want to stop
  // until I reach the top.
  // Baby I'm A*. --Prince
  def aStar(profile: PathfindProfile): TilePath = {
    val startTile = profile.start

    if ( ! startTile.valid) return failure(startTile)

    startNextSearch()
    profile.updateRepulsion()
    val horizon = new mutable.PriorityQueue[TileState]()(Ordering.by(t => -t.totalCostFloor))
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

    while (horizon.nonEmpty) {

      val bestTileState = horizon.dequeue()
      val bestTile = bestTileState.tile
      bestTileState.setVisited()

      // Are we done?
      val atEnd = profile.end.exists(end =>
        end.i == bestTileState.i
        || (
          profile.endDistanceMaximum > 0
          && end.tileDistanceFast(bestTile) <= profile.endDistanceMaximum
          && (profile.crossUnwalkable || end.groundPixels(bestTile) <= 32 * profile.endDistanceMaximum)))
      if (
        profile.lengthMaximum.exists(_ <= Math.round(bestTileState.pathLength)) // Rounding encourages picking diagonal paths for short maximum lengths
        || (
          (atEnd || (profile.end.isEmpty && (profile.endUnwalkable || bestTile.walkableUnchecked)))
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
              neighborState.setPathLength(bestTileState.pathLength + (if (neighborOrthogonal) 1 else PurpleMath.sqrt2f))
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
