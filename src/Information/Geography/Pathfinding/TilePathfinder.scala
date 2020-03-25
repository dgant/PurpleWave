package Information.Geography.Pathfinding

import Debugging.Visualizations.Views.Micro.ShowUnitPaths
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
    var _visitedStamp   : Long  = _stampDefault
    var _enqueuedStamp  : Long  = _stampDefault
    var _cameFrom       : Tile  = _
    var _costFromStart  : Float = _
    var _costToEndFloor : Float = _
    var _pathLength     : Float = _
    var _repulsion      : Double = _
    @inline def setEnqueued() {
      _enqueuedStamp = _stampCurrent
    }
    @inline def setVisited() {
      _visitedStamp = _stampCurrent
    }
    @inline def setCameFrom(value: Tile) {
      _cameFrom = value
    }
    // Cost of best-known path from the start tile.
    // In common A* parlance, this is the gScore.
    @inline def setCostFromStart(value: Float) {
      _costFromStart = value
    }
    // Minimum possible cost to the end.
    // In common A* parlance, this is the fScore.
    @inline def setTotalCostFloor(value: Float) {
      _costToEndFloor = value
    }
    @inline def setPathLength(value: Float) {
      _pathLength = value
    }
    @inline def setRepulsion(value: Double): Unit = {
      _repulsion = value
    }
    @inline def enqueued        : Boolean           = _enqueuedStamp  == _stampCurrent
    @inline def visited         : Boolean           = _visitedStamp  == _stampCurrent
    @inline def cameFrom        : Option[Tile]      = if (enqueued && _cameFrom.i != i) Some(_cameFrom) else None
    @inline def costFromStart   : Float             = _costFromStart
    @inline def totalCostFloor  : Float             = _costToEndFloor
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

  // Best cost from the start tile to this tile.
  // In common A* parlance, this is the gScore.
  @inline private final def costFromStart(
    profile: PathfindProfile,
    toTile: Tile,
    threatGrid: AbstractGridEnemyRange,
    hypotheticalFrom: Option[Tile] = None)
      : Float = {

    val i = toTile.i
    val toState = tiles(i)
    val fromState = hypotheticalFrom.map(t => tiles(t.i)).orElse(toState.cameFrom.map(t => tiles(t.i)))
    if (fromState.isEmpty) return 0
    val fromTile = fromState.get
    val costSoFar     : Float   = fromState.get.costFromStart
    val costDistance  : Float   = if (fromTile.tile.x == toTile.x || fromTile.tile.y == toTile.y) 1f else PurpleMath.sqrt2f
    val costThreat    : Float   = if (profile.costThreat == 0) 0 else profile.costThreat * Math.max(0, threatGrid.getUnchecked(i) - threatGrid.getUnchecked(fromTile.i)) // Max?
    val costOccupancy : Float   = if (profile.costOccupancy == 0)
      0
    else {
      // Intuition: We want to keep this value scaled around 0-1 so we can reason about costOccupancy
      // Signum: Scale-invariant
      // Sigmoid: Tiebreaks equally signed vectors with different scales
      val diff = With.coordinator.gridPathOccupancy.getUnchecked(i) - With.coordinator.gridPathOccupancy.getUnchecked(fromTile.i)
      profile.costOccupancy * 0.5f * (PurpleMath.fastSigmoid(diff) + PurpleMath.signum(diff))
    }
    val costRepulsion : Double  = if (profile.costRepulsion == 0 || profile.maxRepulsion == 0)
      0
    else {
      // Intuition: We want to keep this value scaled around 0-1 so we can reason about costOccupancy
      // Signum: Scale-invariant
      // Sigmoid: Tiebreaks equally signed vectors with different scales
      val diff = toState.repulsion - fromState.get.repulsion
      profile.costRepulsion * 0.5f * (PurpleMath.fastSigmoid(diff) + PurpleMath.signum(diff))
    }

    costSoFar + costDistance + costThreat + costOccupancy + costRepulsion.toFloat
  }

  @inline private final def drawOutput(profile: PathfindProfile, value: TilePath): Unit = {
    if (ShowUnitPaths.mapInUse && profile.unit.exists(u => u.selected || u.transport.exists(_.selected))) {
      profile.unit.foreach(unit =>
        unit.agent.pathBranches = tiles
          .view
          .filter(t => t.enqueued && t.cameFrom.isDefined)
          .map(t => (t.cameFrom.get.pixelCenter, t.tile.pixelCenter)))
    }
  }

  // The A* admissable heuristic: The lowest possible cost to the end of the path.
  // In common parlance, this is h()
  //
  // Threat-aware pathfinding makes it easy to introduce a non-admissible heuristic,
  // so be careful when modifying this.
  @inline private final def costToEndFloor(
    profile: PathfindProfile,
    tile: Tile,
    end: Option[Tile],
    threatGrid: AbstractGridEnemyRange): Float = {

    val i = tile.i

    // We're using "Depth into enemy range" as our threat cost.
    // It's a good metric for measuring progress towards escaping the enemy, but fails to capture how *much* damage they're threatening at each location
    //
    // To escape a tile that's 5 tiles into enemy range means we have to pass through tiles of value 5+4+3+2+1
    // So the floor of the cost we'll pay is the Gaussian expansion of the threat cost at the current tile.

    val costDistanceToEnd   : Float = end.map(end => if (profile.canCrossUnwalkable || ! profile.allowGroundDist) tile.tileDistanceFast(end) else tile.groundPixels(end) / 32.0).getOrElse(0.0).toFloat
    val costOutOfRepulsion  : Float = profile.costRepulsion * PurpleMath.fastSigmoid(tiles(i).repulsion.toFloat) // Hacky; used to smartly tiebreak tiles that are otherwise h() = 0. Using this formulation to minimize likelihood of breaking heuristic requirements
    val costOutOfThreat     : Float = profile.costThreat * threatGrid.getUnchecked(i)

    Math.max(costDistanceToEnd, costOutOfThreat)
  }

  private def failure(tile: Tile) = TilePath(tile, tile, Int.MaxValue, None)

  // I don't want to stop
  // until I reach the top.
  // Baby I'm A*. --Prince
  def aStar(profile: PathfindProfile): TilePath = {
    val startTile = profile.start
    val endTile   = if (profile.canCrossUnwalkable) profile.end else profile.end.map(_.nearestWalkableTerrain)

    if ( ! startTile.valid) return failure(startTile)

    val isFlying = profile.unit.exists(u => u.flying || u.transport.exists(_.flying)) || profile.canCrossUnwalkable
    val isGround = profile.unit.exists(u => ! u.flying)
    val threatGrid: AbstractGridEnemyRange  =
      if (isFlying && isGround)
        With.grids.enemyRangeAirGround
      else if (isFlying)
        With.grids.enemyRangeAir
      else if (isGround)
        With.grids.enemyRangeGround
      else
        With.grids.enemyRangeAirGround
    startNextSearch()
    profile.updateRepulsion()
    val horizon = new mutable.PriorityQueue[TileState]()(Ordering.by(t => -t.totalCostFloor))
    val startTileState = tiles(startTile.i)
    startTileState.setVisited()
    startTileState.setEnqueued()
    startTileState.setCameFrom(startTile)
    startTileState.setRepulsion(totalRepulsion(profile, startTile))
    startTileState.setCostFromStart(costFromStart(profile, startTile, threatGrid))
    startTileState.setTotalCostFloor(costToEndFloor(profile, startTile, endTile, threatGrid))
    startTileState.setPathLength(1)
    horizon += startTileState

    while (horizon.nonEmpty) {

      val bestTileState = horizon.dequeue()
      val bestTile = bestTileState.tile
      bestTileState.setVisited()

      // Are we done?
      val atEnd = endTile.exists(end =>
        end.i == bestTileState.i
        || (
          profile.endDistanceMaximum > 0
          && end.tileDistanceFast(bestTile) <= profile.endDistanceMaximum
          && (profile.canCrossUnwalkable || end.groundPixels(bestTile) <= 32 * profile.endDistanceMaximum)))
      if (
        profile.lengthMaximum.exists(_ <= Math.round(bestTileState.pathLength)) // Rounding encourages picking diagonal paths for short maximum lengths
        || (
          (atEnd || (endTile.isEmpty && (profile.canCrossUnwalkable || bestTile.walkableUnchecked)))
          && profile.lengthMinimum.forall(_ <= bestTileState.pathLength)
          && profile.threatMaximum.forall(_ >= threatGrid.getUnchecked(bestTile.i)))) {
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
            ! neighborState.visited

            // Is the neighbor pathable?
            && (profile.canCrossUnwalkable || With.grids.walkable.getUnchecked(neighborState.i))

            // Can we path from here to the neighbor?
            // (Specifically, if it's a diagonal step, verify that it's achievable)
            && (profile.canCrossUnwalkable
              || neighborOrthogonal
              || With.grids.walkable.getUnchecked(Tile(neighborTile.x, bestTileState.tile.y).i)
              || With.grids.walkable.getUnchecked(Tile(bestTileState.tile.x, neighborTile.y).i))) {

            val wasEnqueued = neighborState.enqueued
            if ( ! wasEnqueued) {
              neighborState.setRepulsion(totalRepulsion(profile, neighborTile))
            }
            val neighborCostFromStart = costFromStart(profile, neighborTile, threatGrid, Some(bestTile))
            if ( ! wasEnqueued || neighborState.costFromStart > neighborCostFromStart) {
              neighborState.setCameFrom(bestTileState.tile)
              neighborState.setCostFromStart(neighborCostFromStart)
              neighborState.setTotalCostFloor(neighborCostFromStart + costToEndFloor(profile, neighborTile, endTile, threatGrid))
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
