package Information.Geography.Pathfinding

import Information.Geography.Pathfinding.Types.TilePath
import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

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

  private val defaultId: Long = Long.MinValue
  private var currentTileStateId: Long = 0L
  private class TileState {
    var visitedId     : Long    = defaultId
    var cameFromValue : Tile    = _
    var cameFromId    : Long    = defaultId
    var costFromValue : Int     = _
    var costFromId    : Long    = defaultId
    var costToValue   : Int     = _
    var costToId      : Long    = defaultId
    def setVisited() {
      visitedId = currentTileStateId
    }
    def setCameFrom(value: Tile) {
      cameFromValue = value
      cameFromId = currentTileStateId
    }
    def setCostFrom(value: Int) {
      costFromValue = value
      costFromId = currentTileStateId
    }
    def setCostTo(value: Int) {
      costToValue = value
      costToId = currentTileStateId
    }
    def getVisited  : Boolean       =     visitedId   == currentTileStateId
    def getCameFrom : Option[Tile]  = if (cameFromId  == currentTileStateId)  Some(cameFromValue) else None
    def getCostFrom : Int           = if (costFromId  == currentTileStateId)  costFromValue       else Int.MaxValue
    def getCostTo   : Int           = if (costToId    == currentTileStateId)  costToValue         else Int.MaxValue
  }

  private var tiles: Array[TileState] = Array.empty
  private def startNextSearch() {
    if (tiles.isEmpty) {
      tiles = Array.fill(With.geography.allTiles.length){ new TileState }
    }
    if (currentTileStateId == Long.MaxValue) {
      for (i <- tiles.indices) { tiles(i) = new TileState }
      currentTileStateId = defaultId
    }
    currentTileStateId += 1
  }

  /////////////////////////////
  // Options for A* variants //
  /////////////////////////////

  val threatCostMultiplier = 2
  def threatCostAt(unit: FriendlyUnitInfo, tile: Tile): Int = {
    threatCostMultiplier * With.grids.enemyRange.get(tile)
  }
  def threatCostMax(unit: FriendlyUnitInfo, tile: Tile): Int = {
    val c = With.grids.enemyRange.get(tile)
    threatCostMultiplier * (
      // Gaussian expansion of N+(N-1)+...+1
      if (c % 2 == 0)
        (c + 1) * (c / 2)
      else
        (c + 2) * (c / 2) + 1
    )
  }

  def goalDistance(end: Tile): (Tile) => Boolean = _ == end
  def goalThreatAware(unit: FriendlyUnitInfo, end: Option[Tile]): (Tile) => Boolean = tile => {
    With.grids.enemyRange.get(tile) <= 0 && end.forall(theEnd => goalDistance(theEnd)(tile) < goalDistance(theEnd)(unit.tileIncludingCenter))
  }

  def costAtDistance(end: Tile): (Tile) => Int = tile => 1
  def costAtThreatAware(unit: FriendlyUnitInfo, end: Option[Tile]): (Tile) => Int = tile => {
     threatCostAt(unit, tile) + end.map(costAtDistance(_)(tile)).getOrElse(0) + With.coordinator.gridPathOccupancy.get(tile) / 3
  }

  def costToDistance(end: Tile): (Tile) => Int = _.tileDistanceManhattan(end)
  def costToThreatAware(unit: FriendlyUnitInfo, end: Option[Tile]): (Tile) => Int = tile => {
    threatCostMax(unit, tile) + end.map(costToDistance(_)(tile)).getOrElse(0)
  }

  def maximumCostDefault: Int = 2 * (With.mapTileWidth + With.mapTileHeight)
  private def failure(tile: Tile) = TilePath(tile, tile, Int.MaxValue, None)

  def aStarBasic(start: Tile, end: Tile, maximumCost: Int = maximumCostDefault): TilePath = {
    if ( ! end.valid) return failure(start)
    aStar(
      start,
      goalDistance(end),
      costAtDistance(end),
      costToDistance(end),
      maximumCost)
  }

  def aStarThreatAware(unit: FriendlyUnitInfo, end: Option[Tile] = None): TilePath = {
    val realEnd = end.filter(_.valid)
    aStar(
      unit.tileIncludingCenter,
      goalThreatAware(unit, realEnd),
      costAtThreatAware(unit, end),
      costToThreatAware(unit, end),
      maximumCostDefault)
  }

  def aStar(
    start: Tile,
    isGoal: (Tile) => Boolean,
    costAt: (Tile) => Int,
    costTo: (Tile) => Int,
    costMaximum : Int)
    : TilePath = {

    // I don't want to stop
    // until I reach the top.
    // Baby I'm A*. --Prince

    if ( ! start.valid) return failure(start)

    startNextSearch()
    val horizon = new mutable.PriorityQueue[Tile]()(Ordering.by(-costTo(_)))
    tiles(start.i).setCostTo(0)
    tiles(start.i).setCostFrom(costTo(start))
    horizon += start

    while (horizon.nonEmpty) {
      val thisTile = horizon.dequeue()
      if ( ! tiles(thisTile.i).getVisited) {
        tiles(thisTile.i).setVisited()
        if (isGoal(thisTile)) {
          return TilePath(start, thisTile, tiles(thisTile.i).getCostFrom, Some(assemblePath(thisTile)))
        }

        val neighbors = thisTile.adjacent4
        var i = 0
        while (i < 4) {
          val neighbor = neighbors(i)
          i += 1

          if (neighbor.valid
            && With.grids.walkable.get(neighbor)
            && ! tiles(neighbor.i).getVisited
            && costTo(thisTile) < costMaximum) {

            horizon += neighbor

            val neighborCostFrom = tiles(thisTile.i).getCostFrom + costAt(thisTile)

            if (neighborCostFrom < tiles(neighbor.i).getCostFrom) {
              val tileState = tiles(neighbor.i)
              tileState.setCameFrom(thisTile)
              tileState.setCostFrom(neighborCostFrom)
              tileState.setCostTo(neighborCostFrom + costTo(neighbor))
            }
          }
        }
      }
    }
    failure(start)
  }
  
  private def assemblePath(end: Tile): IndexedSeq[Tile] = {
    val path = new ArrayBuffer[Tile]
    path += end
    var last = end
    while (tiles(last.i).getCameFrom.isDefined) {
      last = tiles(last.i).getCameFrom.get
      path.append(last)
    }
    path
  }
}
