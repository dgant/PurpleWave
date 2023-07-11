package Information.Geography.Pathfinding

import Information.Geography.Pathfinding.Types.TilePath
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Tile
import Utilities.?

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.language.postfixOps

trait TilePathfinder {

  val stampDefault            : Long = Long.MinValue
  var stampCurrent            : Long = 0L
  private var tilesExplored   : Long = 0
  private val tilesExploredMax: Long = 4 * (With.mapTileWidth + With.mapTileHeight)

  // Performance metrics
  var aStarPathfinds          : Long = 0
  var aStarExplorationMaxed   : Long = 0
  var aStarOver1ms            : Long = 0
  var aStarNanosMax           : Long = 0
  var aStarNanosTotal         : Long = 0
  var aStarPathLengthMax      : Long = 0
  var aStarPathLengthTotal    : Long = 0
  var aStarTilesExploredMax   : Long = 0
  var aStarTilesExploredTotal : Long = 0

  def profileDistance(start: Tile, end: Tile): PathfindProfile = new PathfindProfile(start, Some(end))
  private var tiles: Array[TileState] = Array.empty
  private val horizon = new mutable.PriorityQueue[TileState]()(Ordering.by(t => - t.costToEndFloor))

  @inline private final def totalRepulsion(profile: PathfindProfile, tile: Tile): Double = {
    var output  = 0.0
    var i       = 0
    val length  = profile.repulsors.length
    while (i < length) {
      val repsulsor =   profile.repulsors(i)
      val distance  =   repsulsor.source.pixelDistance(tile.center)
      output        +=  repsulsor.magnitude * (1 + repsulsor.rangePixels) / (1 + distance)
      i             +=  1
    }
    output
  }

  // Best cost from the start tile to this tile.
  // In common A* parlance, this is the gScore.
  val maxMobility     : Double = 6.0
  val invMaxMobility  : Double = 1 / maxMobility
  @inline private final def costFromStart(profile: PathfindProfile, toTile: Tile, hypotheticalFrom: Option[Tile] = None): Double = {
    val i                         = toTile.i
    val toState                   = tiles(i)
    var fromState                 = hypotheticalFrom.map(t => tiles(t.i)).orElse(toState.cameFrom.map(t => tiles(t.i)))
    if (fromState.isEmpty) return 0
    val fromTile                  = fromState.get
    val costSoFar       : Double  = fromState.get.costFromStart
    val costDistance    : Double  = if (fromTile.tile.x == toTile.x || fromTile.tile.y == toTile.y) 1.0 else Maff.sqrt2
    val costEnemyVision : Double  = if (toTile.visibleToEnemy) profile.costEnemyVision else 0.0
    val costImmobility  : Double  = if (profile.costImmobility  == 0) 0.0 else profile.costImmobility * Math.max(0.0, maxMobility - toTile.mobility) * invMaxMobility
    val costOccupancy   : Double  = if (profile.costOccupancy   == 0) 0.0 else {
      // Intuition: We want to keep this value scaled around 0-1 so we can reason about costOccupancy
      // Signum: Scale-invariant
      // Sigmoid: Tiebreaks equally signed vectors with different scales
      val diff = With.coordinator.gridPathOccupancy.getUnchecked(i) - With.coordinator.gridPathOccupancy.getUnchecked(fromTile.i)
      profile.costOccupancy * 0.5 * (Maff.fastSigmoid01(diff) + 0.5 + 0.5 * Maff.signum101(diff))
    }
    val costRepulsion: Double = if (profile.costRepulsion == 0 || profile.maxRepulsion == 0) 0 else {
      // Intuition: We want to keep this value scaled around 0-1 so we can reason about costRepulsion
      // and it must be non-negative to preserve heuristic admissibility
      // Signum: Scale-invariant
      // Sigmoid: Tiebreaks equally signed vectors with different scales
      val diff = toState.repulsion - fromState.get.repulsion
      profile.costRepulsion * 0.5 * (Maff.fastSigmoid01(diff) + 0.5 + 0.5 * Maff.signum101(diff))
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
    // We're using "Depth into enemy range" as our threat cost.
    // It's a good metric for measuring progress towards escaping the enemy, but fails to capture how *much* damage they're threatening at each location
    //
    // To escape a tile that's 5 tiles into enemy range means we have to pass through tiles of value 5+4+3+2+1
    // So the floor of the cost we'll pay is the Gaussian expansion of the threat cost at the current tile.

    // Added costOutOfRepulsion at some point but never tested integrating it
    //val costOutOfRepulsion  : Double = profile.costRepulsion * Maff.fastSigmoid01(tiles(tile.i).repulsion) // Hacky; used to smartly tiebreak tiles that are otherwise h() = 0. Using this formulation to minimize likelihood of breaking heuristic requirements

    val costDistanceToEnd: Double = profile.end.map(end =>
      ?(profile.crossUnwalkable || ! profile.employGroundDist,
        tile.tileDistanceFast(end),
        tile.tileDistanceFast(end) * 0.1 + tile.groundPixels(end) * Maff.inv32))
      .getOrElse(0.0)

    val costOutOfThreat: Double = profile.costThreat * profile.threatGrid.getUnchecked(tile.i)

    Math.max(costDistanceToEnd, costOutOfThreat)
  }

  private def failure(tile: Tile) = TilePath(tile, tile, Int.MaxValue, None)

  def aStar(from: Tile, to: Tile): TilePath = new PathfindProfile(from, Some(to)).find

  // I don't want to stop
  // until I reach the top.
  // Baby I'm A*. --Prince
  def aStar(profile: PathfindProfile): TilePath = {
    val nanosBefore          = System.nanoTime()
    val output               = aStarInner(profile)
    val nanosDelta           = Math.max(0, System.nanoTime() - nanosBefore)
    val pathLength           = output.tiles.map(_.length).getOrElse(0)
    aStarPathfinds          += 1
    aStarExplorationMaxed   += Maff.fromBoolean(tilesExplored >= tilesExploredMax)
    aStarOver1ms            += Maff.fromBoolean(nanosDelta > 1e6)
    aStarNanosMax            = Math.max(aStarNanosMax, nanosDelta)
    aStarNanosTotal         += nanosDelta
    aStarPathLengthMax       = Math.max(aStarPathLengthMax, pathLength)
    aStarPathLengthTotal    += pathLength
    aStarTilesExploredMax    = Math.max(aStarTilesExploredMax, tilesExplored)
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
    val startTileState        = tiles(startTile.i)
    val alsoUnwalkableI       = profile.alsoUnwalkable.map(_.i)
    val endExists             = profile.end.isDefined
    val endI                  = profile.end.map(_.i).getOrElse(Int.MinValue)
    val endTile               = profile.end.getOrElse(Tile(0, 0))
    val endDistanceMaxTiles   = profile.endDistanceMaximum
    val endDistanceMaxPixels  = endDistanceMaxTiles * 32
    val lengthMinimum         = profile.lengthMinimum.getOrElse(Double.NegativeInfinity)
    val lengthMaximum         = profile.lengthMaximum.getOrElse(Double.PositiveInfinity)
    val threatMaximum         = profile.threatMaximum.getOrElse(Int.MaxValue)
    val repulsion             = profile.repulsors.nonEmpty
    startTileState.setVisited()
    startTileState.setEnqueued()
    startTileState.setCameFrom        (startTile)
    startTileState.setRepulsion       (totalRepulsion(profile, startTile))
    startTileState.setCostFromStart   (costFromStart(profile, startTile))
    startTileState.setCostToEndFloor  (costToEndFloor(profile, startTile))
    startTileState.setPathLength      (1)
    horizon += startTileState
    tilesExplored = 0

    while (horizon.nonEmpty && tilesExplored < tilesExploredMax) {
      tilesExplored += 1
      val bestTileState = horizon.dequeue()
      val bestTile      = bestTileState.tile
      bestTileState.setVisited()

      // Are we done?
      val atProfileEnd = endExists && (endI == bestTileState.i || (endDistanceMaxTiles > 0 && endTile.tileDistanceFast(bestTile) <= endDistanceMaxTiles && (profile.crossUnwalkable || endTile.groundPixels(bestTile) <= endDistanceMaxPixels)))
      if (
        (lengthMaximum <= bestTileState.pathLength + 1 && lengthMaximum <= Math.round(bestTileState.pathLength)) // Rounding encourages picking diagonal paths for short maximum lengths
        || (profile.acceptPartialPath && tilesExplored >= tilesExploredMax)
        || (
          (atProfileEnd || ( ! endExists && (profile.endUnwalkable || bestTile.walkableUnchecked)))
          && lengthMinimum  <= bestTileState.pathLength
          && threatMaximum  >= profile.threatGrid.getUnchecked(bestTile.i))) {

        val output = TilePath(
          startTile,
          bestTile,
          costFromStart(profile, bestTileState.tile),
          Some(assemblePath(bestTileState)))

        profile.unit
          .flatMap(_.friendly)
          .map(_.agent)
          .foreach(_.lastPath = Some(output))

        return output
      }

      val neigborTiles = bestTileState.tile.adjacent8
      var i = 0
      while (i < 8) {

        val neighborTile = neigborTiles(i)
        if (neighborTile.valid) {

          val neighborState = tiles(neighborTile.i)
          if ( ! neighborState.visited && (profile.crossUnwalkable || (neighborState.tile.walkableUnchecked && ! alsoUnwalkableI.contains(neighborTile.i)))) {

            lazy val neighborOrthogonal = neighborTile.x == bestTile.x || neighborTile.y == bestTile.y
            if (profile.crossUnwalkable || (
                neighborOrthogonal
                  || With.grids.walkable.getUnchecked(neighborTile.x,       bestTileState.tile.y)
                  || With.grids.walkable.getUnchecked(bestTileState.tile.x, neighborTile.y))) {

              val wasEnqueued = neighborState.enqueued
              if (repulsion && ! wasEnqueued) {
                neighborState.setRepulsion(totalRepulsion(profile, neighborTile))
              }
              val costFromStartHere = costFromStart(profile, neighborTile, Some(bestTile))
              if ( ! wasEnqueued || neighborState.costFromStart > costFromStartHere) {
                neighborState.setCameFrom       (bestTileState.tile)
                neighborState.setCostFromStart  (costFromStartHere)
                neighborState.setCostToEndFloor (costFromStartHere + costToEndFloor(profile, neighborTile))
                neighborState.setPathLength     (bestTileState.pathLength + (if (neighborOrthogonal) 1 else Maff.sqrt2))
              }
              if ( ! wasEnqueued) {
                neighborState.setEnqueued()
                horizon += neighborState
              }
            }
          }
        }
        i += 1
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
