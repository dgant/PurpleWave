package Information.Geography.Pathfinding

import Information.Geography.Pathfinding.Types.TilePath
import Information.Grids.Combat.AbstractGridEnemyRange
import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.language.postfixOps

trait Pathfinder {

  val impossiblyLargeDistance: Long = 32L * 32L * 256L * 256L * 100L
  
  def groundPathExists(origin: Tile, destination: Tile): Boolean = {
    origin.zone == destination.zone || groundTiles(origin, destination) < impossiblyLargeDistance
  }

  def groundTilesManhattan(origin: Tile, destination: Tile): Long = {
    // Some maps have broken ground distance (due to continued reliance on BWTA,
    // which in particular seems to suffer on maps with narrow ramps, eg. Plasma, Third World
    if (With.strategy.map.exists( ! _.trustGroundDistance)) {
      return origin.tileDistanceManhattan(destination)
    }

    // Let's first check if we can use air distance. It's cheaper and more accurate.
    // We can "get away" with using air distance if we're in the same zone
    if (origin.zone == destination.zone) {
      return origin.tileDistanceManhattan(destination)
    }

    // This approximation -- calculating ground distance at tile resolution -- can potentially bite us.
    // Pun intended on "potentially" -- the risk here is using it for potential fields near a chokepoint
    // before which we're getting pixel-resolution distance and after which we're getting tile-resolution distance
    Math.max(
      origin.tileDistanceManhattan(destination),
      groundTiles(origin, destination))
  }
  
  def groundPixels(origin: Pixel, destination: Pixel): Double = {
    // Some maps have broken ground distance (due to continued reliance on BWTA,
    // which in particular seems to suffer on maps with narrow ramps, eg. Plasma, Third World
    if (With.strategy.map.exists( ! _.trustGroundDistance)) {
      return origin.pixelDistance(destination)
    }
    
    // Let's first check if we can use air distance. It's cheaper and more accurate.
    // We can "get away" with using air distance if we're in the same zone
    if (origin.zone == destination.zone) {
      return origin.pixelDistance(destination)
    }
    
    // This approximation -- calculating ground distance at tile resolution -- can potentially bite us.
    // Pun intended on "potentially" -- the risk here is using it for potential fields near a chokepoint
    // before which we're getting pixel-resolution distance and after which we're getting tile-resolution distance
    Math.max(
      origin.pixelDistance(destination),
      32 * groundTiles(origin.tileIncluding, destination.tileIncluding))
  }

  protected def groundTiles(origin: Tile, destination: Tile): Long = {
    ByOption
      .min(destination.zone.edges.map(edge =>
        edge.distanceGrid.get(destination) + edge.distanceGrid.get(origin).toLong))
      .getOrElse(impossiblyLargeDistance)
  }
  
  ////////////////////////////////////////////////////////////
  // From the old GroundPathFinder -- this can be split out //
  ////////////////////////////////////////////////////////////

  def profileDistance(start: Tile, end: Tile) = PathfindProfile(start, Some(end))
  def profileThreatAware(start: Tile, end: Option[Tile], goalDistance: Int, flying: Boolean) = PathfindProfile(
    start,
    end,
    flying = flying,
    costOccupancy = 1f,
    costThreat = 1f)

  private val _visitationIdDefault: Long = Long.MinValue
  private var _visitationIdCurrent: Long = 0L
  private class TileState(val tile: Tile) {
    val i: Int = tile.i
    var visitedId     : Long    = _visitationIdDefault
    var cameFromValue : Tile    = _
    var cameFromId    : Long    = _visitationIdDefault
    var costFrom      : Float   = _
    var costFromId    : Long    = _visitationIdDefault
    var costTo        : Float   = _
    var costToId      : Long    = _visitationIdDefault
    /*
    def setVisited() {
      visitedId = _visitationIdCurrent
    }
    def setCameFrom(value: Tile) {
      cameFromValue = value
      cameFromId = _visitationIdCurrent
    }
    def setCostFrom(value: Float) {
      costFromValue = value
      costFromId = _visitationIdCurrent
    }
    def setCostTo(value: Float) {
      costToValue = value
      costToId = _visitationIdCurrent
    }
    def getVisited  : Boolean       =     visitedId   == _visitationIdCurrent
    def getCameFrom : Option[Tile]  = if (cameFromId  == _visitationIdCurrent)  Some(cameFromValue) else None
    def getCostFrom : Float         = if (costFromId  == _visitationIdCurrent)  costFromValue       else Float.MaxValue
    def getCostTo   : Float         = if (costToId    == _visitationIdCurrent)  costToValue         else Float.MaxValue

    */
  }

  private var tiles: Array[TileState] = Array.empty
  private def startNextSearch() {
    if (tiles.isEmpty || _visitationIdCurrent == Long.MaxValue) {
      tiles = With.geography.allTiles.indices.map(i => new TileState(new Tile(i))).toArray
    }
    if (_visitationIdCurrent == Long.MaxValue) {
      _visitationIdCurrent = _visitationIdDefault
    }
    _visitationIdCurrent += 1
  }

  @inline
  private final def costToEnd(
    profile: PathfindProfile,
    tile: Tile,
    threatGrid: AbstractGridEnemyRange)
    : Float = {

    val i = tile.i
    (
      profile.end.getOrElse(tile).tileDistanceManhattan(tile)
      + (if (profile.costThreat == 0) 0 else
          if (threatGrid.framestamps.rawValues(i) < threatGrid.framestamps.version) 0
          else threatGrid.rawValues(i) * profile.costThreat)
      + (if (profile.costOccupancy == 0) 0 else
          if (With.coordinator.gridPathOccupancy.framestamps.rawValues(i) < With.coordinator.gridPathOccupancy.framestamps.version) 0
          else With.coordinator.gridPathOccupancy.rawValues(i))
    )
  }

  def aStar(profile: PathfindProfile): TilePath = {
    val start = profile.start
    val end = profile.end.map(_.i).getOrElse(-1)
    val costMaximum = profile.maximumCost.getOrElse(Math.sqrt(Float.MaxValue))
    val costMaximumSquared = costMaximum * costMaximum
    val threatGrid = if (profile.flying) With.grids.enemyRangeAir else With.grids.enemyRangeGround

    // I don't want to stop
    // until I reach the top.
    // Baby I'm A*. --Prince

    if ( ! start.valid) return TilePath(start, start, Int.MaxValue, None)

    startNextSearch()
    val horizon = new mutable.PriorityQueue[TileState]()(Ordering.by(t =>
      - (if (t.costToId == _visitationIdCurrent) t.costTo else Float.MaxValue)
      - (if (t.costFromId == _visitationIdCurrent) t.costFrom else Float.MaxValue)))
    val startTileState        = tiles(start.i)
    startTileState.visitedId  = _visitationIdCurrent
    startTileState.costToId   = _visitationIdCurrent
    startTileState.costFromId = _visitationIdCurrent
    startTileState.costTo     = 0
    startTileState.costFrom   = costToEnd(profile, start, threatGrid)
    horizon += startTileState

    while (horizon.nonEmpty) {
      val thisTileState = horizon.dequeue()
      if (thisTileState.visitedId != _visitationIdCurrent) {
        thisTileState.visitedId = _visitationIdCurrent
        if (profile.end.exists(_.i == thisTileState.i) || profile.maximumCost.exists(_ <= thisTileState.tile.tileDistanceManhattan(start))) {
          return TilePath(
            start,
            thisTileState.tile,
            thisTileState.costFrom,
            Some(assemblePath(thisTileState)))
        }

        val neighbors = thisTileState.tile.adjacent8
        var i = 0
        while (i < 8) {
          val neighbor = neighbors(i)
          i += 1

          if (neighbor.valid
            && With.grids.walkable.get(neighbor)
            && tiles(neighbor.i).visitedId < _visitationIdCurrent
            && thisTileState.costTo < costMaximum
            // Verify diagonal walkability
            && (neighbor.x == thisTileState.tile.x
              || neighbor.y == thisTileState.tile.y
              || With.grids.walkable.get(Tile(neighbor.x, thisTileState.tile.y))
              || With.grids.walkable.get(Tile(thisTileState.tile.x, neighbor.y)))) {

            val neighborCostFrom = thisTileState.costFrom + costToTile(thisTileState)

            if (neighborCostFrom < tiles(neighbor.i).costFrom) {
              val tileState = tiles(neighbor.i)
              tileState.setCameFrom(thisTileState)
              tileState.setCostFrom(neighborCostFrom)
              tileState.setCostTo(neighborCostFrom + costToEnd(neighbor))
            }

            horizon += neighbor
          }
        }
      }
    }
    failure(start)
  }
  
  private def assemblePath(end: TileState): IndexedSeq[Tile] = {
    val path = new ArrayBuffer[Tile]
    path += end.tile
    var last = end.tile
    while (tiles(last.i).getCameFrom.isDefined) {
      last = tiles(last.i).getCameFrom.get
      path.append(last)
    }
    path
  }
}
