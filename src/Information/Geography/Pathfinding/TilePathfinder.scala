package Information.Geography.Pathfinding

import Debugging.Visualizations.Views.Micro.ShowUnitsFriendly
import Information.Geography.Pathfinding.Types.TilePath
import Information.Grids.Combat.AbstractGridEnemyRange
import Lifecycle.With
import Mathematics.Points.Tile
import Mathematics.PurpleMath

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.language.postfixOps

trait TilePathfinder {

  def profileDistance(start: Tile, end: Tile): PathfindProfile = new PathfindProfile(start, Some(end))

  private val _stampDefault: Long = Long.MinValue
  private var _stampCurrent: Long = 0L
  private var tiles: Array[TileState] = Array.empty

  private final class TileState(val tile: Tile) {
    val i: Int = tile.i
    var _enqueuedStamp  : Long  = _stampDefault
    var _cameFrom       : Tile  = _
    var _costFromStart  : Float = _
    var _costToEndFloor : Float = _
    var _pathLength     : Float = _
    var _repulsion      : Double = _
    @inline def setEnqueued() {
      _enqueuedStamp = _stampCurrent
    }
    @inline def setCameFrom(value: Tile) {
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
    @inline def setRepulsion(value: Double): Unit = {
      _repulsion = value
    }
    @inline def enqueued        : Boolean           = _enqueuedStamp  == _stampCurrent
    @inline def cameFrom        : Option[Tile]      = if (enqueued && _cameFrom.i != i) Some(_cameFrom) else None
    @inline def costFromStart   : Float             = _costFromStart
    @inline def costToEndFloor  : Float             = _costToEndFloor
    @inline def pathLength      : Float             = _pathLength
    @inline def repulsion       : Double            = _repulsion
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

  // Best cost from the start to this tile
  @inline private final def costFromStart(
    profile: PathfindProfile,
    toTile: Tile,
    threatGrid: AbstractGridEnemyRange)
      : Float = {

    val i = toTile.i
    val toState = tiles(i)
    val fromState = toState.cameFrom.map(t => tiles(t.i))
    if (fromState.isEmpty) return 0
    val fromTile = fromState.get
    val costSoFar     : Float   = fromState.get.costToEndFloor
    val costDistance  : Float   = if (fromTile.tile.x == toTile.x || fromTile.tile.y == toTile.y) 1f else PurpleMath.sqrt2f
    val costThreat    : Float   = if (profile.costThreat == 0) 0 else profile.costThreat * Math.max(0, threatGrid.getUnchecked(i) - threatGrid.getUnchecked(fromTile.i)) // Max?
    val costOccupancy : Float   = if (profile.costOccupancy == 0)
      0
    else
      profile.costOccupancy * Math.max(-0.1f, PurpleMath.signum(
          With.coordinator.gridPathOccupancy.getUnchecked(i)
        - With.coordinator.gridPathOccupancy.getUnchecked(fromTile.i)))
    val costRepulsion : Double  = if (profile.costRepulsion == 0 || profile.maxRepulsion == 0)
      0
    else
      profile.costRepulsion * Math.max(-0.1, PurpleMath.signum(
          toState.repulsion
          - fromState.get.repulsion))

    costSoFar + costDistance + costThreat + costOccupancy + costRepulsion.toFloat
  }

  @inline private final def drawOutput(profile: PathfindProfile, value: TilePath): Unit = {
    if (ShowUnitsFriendly.mapInUse && ShowUnitsFriendly.showPaths && profile.unit.exists(_.selected)) {
      profile.unit.foreach(unit =>
        unit.agent.pathBranches = tiles
          .view
          .filter(t => t.enqueued && t.cameFrom.isDefined)
          .map(t => (t.cameFrom.get.pixelCenter, t.tile.pixelCenter)))
    }
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

    val costDistanceToEnd   : Float = profile.end.map(end => if (profile.flying || ! profile.allowGroundDist) tile.tileDistanceFast(end) else tile.groundPixels(end) / 32).getOrElse(0.0).toFloat
    val costOutOfThreat     : Float = profile.costThreat * threatGrid.getUnchecked(i)

    Math.max(costDistanceToEnd, costOutOfThreat)
  }

  private def failure(tile: Tile) = TilePath(tile, tile, Int.MaxValue, None)

  // I don't want to stop
  // until I reach the top.
  // Baby I'm A*. --Prince
  def aStar(profile: PathfindProfile): TilePath = {
    val startTile = profile.start

    if ( ! startTile.valid) return failure(startTile)

    val threatGrid = if (profile.flying) With.grids.enemyRangeAir else With.grids.enemyRangeGround
    startNextSearch()
    profile.updateRepulsion()
    val horizon = new mutable.PriorityQueue[TileState]()(Ordering.by(t => -t.costToEndFloor))
    val startTileState = tiles(startTile.i)
    startTileState.setEnqueued()
    startTileState.setCameFrom(startTile)
    startTileState.setRepulsion(totalRepulsion(profile, startTile))
    startTileState.setCostFromStart(costFromStart(profile, startTile, threatGrid))
    startTileState.setCostToEndFloor(costToEndFloor(profile, startTile, threatGrid))
    startTileState.setPathLength(1)
    horizon += startTileState

    while (horizon.nonEmpty) {

      val bestTileState = horizon.dequeue()
      val bestTile = bestTileState.tile

      // Are we done?
      if (profile.end.exists(_.i == bestTileState.i)
        || profile.maximumLength.exists(_ <= bestTileState.pathLength)
        || (
          profile.end.isEmpty
          && profile.maximumLength.isEmpty
          && threatGrid.getUnchecked(bestTile.i) == 0)) {
        val output = TilePath(
          startTile,
          bestTile,
          costFromStart(profile, bestTileState.tile, threatGrid),
          Some(assemblePath(bestTileState)))
        drawOutput(profile, output)
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
            neighborState.setEnqueued()
            neighborState.setCameFrom(bestTileState.tile)
            neighborState.setRepulsion(totalRepulsion(profile, neighborTile))
            val neighborCostFromStart = costFromStart(profile, neighborTile, threatGrid)
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
