package Information.Geography.Pathfinding

import Information.Geography.Pathfinding.Types.TilePath
import Information.Grids.Combat.AbstractGridEnemyRange
import Lifecycle.With
import Mathematics.Points.Tile
import Mathematics.PurpleMath

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.language.postfixOps

trait TilePathfinder {
  
  ////////////////////////////////////////////////////////////
  // From the old GroundPathFinder -- this can be split out //
  ////////////////////////////////////////////////////////////

  def profileDistance(start: Tile, end: Tile): PathfindProfile = {
    PathfindProfile(start, Some(end))
  }
  def profileThreatAware(start: Tile, end: Option[Tile], maximumLength: Int, flying: Boolean): PathfindProfile = {
    PathfindProfile(
      start,
      end,
      flying = flying,
      maximumLength = Some(maximumLength),
      costOccupancy = 1f,
      costThreat = 4f)
  }

  private val _stampDefault: Long = Long.MinValue
  private var _stampCurrent: Long = 0L
  private var tiles: Array[TileState] = Array.empty

  private final class TileState(val tile: Tile) {
    val i: Int = tile.i
    var _visitedStamp         : Long  = _stampDefault
    var _enqueuedStamp        : Long  = _stampDefault
    var _cameFromStamp        : Long  = _stampDefault
    var _cameFrom             : Tile  = _
    var _costFromStart        : Float = _
    var _costToEndFloor       : Float = _
    var _pathLength           : Float = _
    @inline def setVisited() {
      _visitedStamp = _stampCurrent
    }
    @inline def setEnqueued() {
      _enqueuedStamp = _stampCurrent
    }
    @inline def setCameFrom(value: Tile) {
      _cameFromStamp = _stampCurrent
      _cameFrom = value
    }
    @inline def setCostFromStart(value: Float) {
      _costFromStart = value
    }
    @inline def setCostToEndFloor(value: Float) {
      _costToEndFloor = value
    }
    @inline def setPathLength(value: Float) {
      _pathLength = value
    }
    @inline def visited         : Boolean           =     _visitedStamp   == _stampCurrent
    @inline def enqueued        : Boolean           =     _enqueuedStamp  == _stampCurrent
    @inline def cameFrom        : Option[Tile]      = if (_cameFromStamp == _stampCurrent) Some(_cameFrom) else None
    @inline def cameFromState   : Option[TileState] = cameFrom.map(t => tiles(t.i))
    @inline def costFromStart   : Float             = if (enqueued) _costFromStart    else Float.MaxValue
    @inline def costToEndFloor  : Float             = if (enqueued) _costToEndFloor   else Float.MaxValue
    @inline def pathLength      : Float             = if (enqueued) _pathLength       else Float.MaxValue
  }

  private def startNextSearch() {
    if (tiles.isEmpty || _stampCurrent == Long.MaxValue) {
      tiles = With.geography.allTiles.indices.map(i => new TileState(new Tile(i))).toArray
    }
    if (_stampCurrent == Long.MaxValue) {
      _stampCurrent = _stampDefault
    }
    _stampCurrent += 1
  }

  // Best-known cost from the start to this tile
  @inline private final def costFromStart(
    profile: PathfindProfile,
    to: Tile,
    threatGrid: AbstractGridEnemyRange)
      : Float = {

    val i = to.i
    val from = tiles(i).cameFromState
    if (from.isEmpty) return 0
    val f = from.get
    val costSoFar     : Float = tiles(f.i).costToEndFloor
    val costDistance  : Float = if (f.tile.x == to.x || f.tile.y == to.y) 1f else PurpleMath.sqrt2f
    val costThreat    : Float = if (profile.costThreat == 0) 0 else profile.costThreat * Math.max(0, threatGrid.getUnchecked(i) - threatGrid.getUnchecked(f.i)) // Max?
    val costOccupancy : Float = if (profile.costOccupancy == 0) 0 else profile.costOccupancy * With.coordinator.gridPathOccupancy.getUnchecked(i)
    costSoFar + costDistance + costThreat + costOccupancy
  }

  // The A* admissable heuristic: The lowest possible cost to the end of the path.
  //
  // Threat-aware pathfinding makes it easy to introduce a non-admissible heuristic,
  // so be careful when modifying this (or how the threat grid is defined).
  @inline private final def costToEndFloor(
    profile: PathfindProfile,
    tile: Tile,
    threatGrid: AbstractGridEnemyRange): Float = {

    val i = tile.i

    // We're using "Depth into enemy range" as our threat cost.
    // It's a good metric for measuring progress towards escaping the enemy, but fails to capture how *much* damage they're threatening at each location
    //
    // To escape a tile that's 5 tiles into enemy range means we have to pass through tiles of value 5+4+3+2+1
    // So the floor of the cost we'll pay is the Gaussian expansion of the threat cost at the current tile.

    val costDistanceToEnd   : Float = profile.end.map(tile.tileDistanceFast(_).toFloat).getOrElse(0f)
    val costOutOfThreat     : Float = profile.costThreat * threatGrid.getUnchecked(i)
    val costFloorOccupancy  : Float = profile.costOccupancy * With.coordinator.gridPathOccupancy.getUnchecked(i)

    costFloorOccupancy + Math.max(costDistanceToEnd, costOutOfThreat)
  }

  private def failure(tile: Tile) = TilePath(tile, tile, Int.MaxValue, None)

  def aStar(profile: PathfindProfile): TilePath = {
    val startTile = profile.start
    val costMaximum = profile.maximumLength.getOrElse(Float.MaxValue)
    val threatGrid = if (profile.flying) With.grids.enemyRangeAir else With.grids.enemyRangeGround

    // I don't want to stop
    // until I reach the top.
    // Baby I'm A*. --Prince

    if ( ! startTile.valid) return failure(startTile)

    startNextSearch()
    val horizon = new mutable.PriorityQueue[TileState]()(Ordering.by(t => -t.costToEndFloor))
    val startTileState = tiles(startTile.i)
    startTileState.setVisited()
    startTileState.setEnqueued()
    startTileState.setCostFromStart(costFromStart(profile, startTile, threatGrid))
    startTileState.setCostToEndFloor(costToEndFloor(profile, startTile, threatGrid))
    startTileState.setPathLength(1)
    horizon += startTileState

    while (horizon.nonEmpty) {

      val bestTileState = horizon.dequeue()
      val bestTile = bestTileState.tile
      tiles(bestTileState.i).setVisited()

      // Are we done?
      if (profile.end.exists(_.i == bestTileState.i)
        || profile.maximumLength.exists(_ <= bestTileState.pathLength)
        || (
          profile.end.isEmpty
          && profile.maximumLength.isEmpty
          && threatGrid.getUnchecked(bestTile.i) == 0)) {
        return TilePath(
          startTile,
          bestTile,
          costFromStart(profile, bestTileState.tile, threatGrid),
          Some(assemblePath(bestTileState)))
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
            // If we've used an admissible heuristic,
            // and have previously enqueued the neighbor into our horizon,
            // then it is impossible for us to have approached it via a superior path,
            // and thus there's no point modifying its cost-to-end floor.
            ! neighborState.enqueued

            // Is the neighbor pathable?
            // TODO: Recursively inline and make unchecked
            && (profile.flying || With.grids.walkable.getUnchecked(neighborState.i))

            // Can we path from here to the neighbor?
            // (Specifically, if it's a diagonal step, verify that it's achievable)
            && (profile.flying
              || neighborTile.x == bestTileState.tile.x
              || neighborTile.y == bestTileState.tile.y
              || With.grids.walkable.getUnchecked(Tile(neighborTile.x, bestTileState.tile.y).i)
              || With.grids.walkable.getUnchecked(Tile(bestTileState.tile.x, neighborTile.y).i))) {

            val neighborState = tiles(neighborTile.i)
            val neighborCostFromStart = costFromStart(profile, neighborTile, threatGrid)
            neighborState.setEnqueued()
            neighborState.setCameFrom(bestTileState.tile)
            neighborState.setCostFromStart(neighborCostFromStart)
            neighborState.setCostToEndFloor(neighborCostFromStart + costToEndFloor(profile, neighborTile, threatGrid))
            neighborState.setPathLength(bestTileState.pathLength + (if (neighborOrthogonal) 1 else PurpleMath.sqrt2f))
            horizon += neighborState
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
