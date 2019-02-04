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
    var costFromValue : Float   = _
    var costFromId    : Long    = defaultId
    var costToValue   : Float   = _
    var costToId      : Long    = defaultId
    def setVisited() {
      visitedId = currentTileStateId
    }
    def setCameFrom(value: Tile) {
      cameFromValue = value
      cameFromId = currentTileStateId
    }
    def setCostFrom(value: Float) {
      costFromValue = value
      costFromId = currentTileStateId
    }
    def setCostTo(value: Float) {
      costToValue = value
      costToId = currentTileStateId
    }
    def getVisited  : Boolean       =     visitedId   == currentTileStateId
    def getCameFrom : Option[Tile]  = if (cameFromId  == currentTileStateId)  Some(cameFromValue) else None
    def getCostFrom : Float         = if (costFromId  == currentTileStateId)  costFromValue       else Float.MaxValue
    def getCostTo   : Float         = if (costToId    == currentTileStateId)  costToValue         else Float.MaxValue
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
  def threatCostAt(unit: FriendlyUnitInfo, tile: Tile): Float = {
    threatCostMultiplier * unit.enemyRangeGrid.get(tile)
  }
  def threatCostMax(unit: FriendlyUnitInfo, tile: Tile): Float = {
    val c = unit.enemyRangeGrid.get(tile)
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
    unit.enemyRangeGrid.get(tile) <= 0 && end.forall(theEnd => goalDistance(theEnd)(tile) < goalDistance(theEnd)(unit.tileIncludingCenter))
  }

  private val sqrt2f: Float = Math.sqrt(2).toFloat
  def costToTileDistance(to: Tile): (Tile) => Float = from => if (from.x == to.x || from.y == to.y) 1f else sqrt2f
  def costToTileAware(unit: FriendlyUnitInfo, to: Option[Tile]): (Tile) => Float = from => {
     threatCostAt(unit, from) + to.map(costToTileDistance(_)(from)).getOrElse(0f) + With.coordinator.gridPathOccupancy.get(from) / 3f
  }

  def costToEndDistance(end: Tile): (Tile) => Float = _.tileDistanceFast(end).toInt
  def costToEndThreatAware(unit: FriendlyUnitInfo, end: Option[Tile]): (Tile) => Float = tile => {
    threatCostMax(unit, tile) + end.map(costToEndDistance(_)(tile)).getOrElse(0f)
  }

  val maximumCostDefault: Float = 2 * (With.mapTileWidth + With.mapTileHeight)
  private def failure(tile: Tile) = TilePath(tile, tile, Int.MaxValue, None)

  def aStarBasic(start: Tile, end: Tile, maximumCost: Float = maximumCostDefault): TilePath = {
    if ( ! end.valid) return failure(start)
    aStar(
      start,
      goalDistance(end),
      costToTileDistance(end),
      costToEndDistance(end),
      maximumCost)
  }

  def aStarThreatAware(unit: FriendlyUnitInfo, end: Option[Tile] = None): TilePath = {
    val realEnd = end.filter(_.valid)
    aStar(
      unit.tileIncludingCenter,
      goalThreatAware(unit, realEnd),
      costToTileAware(unit, end),
      costToEndThreatAware(unit, end),
      maximumCostDefault)
  }

  def aStar(
    start       : Tile,
    isGoal      : (Tile) => Boolean,
    costToTile  : (Tile) => Float,
    costToEnd   : (Tile) => Float,
    costMaximum : Float)
    : TilePath = {

    // I don't want to stop
    // until I reach the top.
    // Baby I'm A*. --Prince

    if ( ! start.valid) return failure(start)

    startNextSearch()
    val horizon = new mutable.PriorityQueue[Tile]()(Ordering.by(t => -costToEnd(t) - tiles(t.i).getCostFrom))
    tiles(start.i).setCostTo(0)
    tiles(start.i).setCostFrom(costToEnd(start))
    horizon += start

    while (horizon.nonEmpty) {
      val thisTile = horizon.dequeue()
      if ( ! tiles(thisTile.i).getVisited) {
        tiles(thisTile.i).setVisited()
        if (isGoal(thisTile)) {
          return TilePath(
            start,
            thisTile,
            tiles(thisTile.i).getCostFrom,
            Some(assemblePath(thisTile)))
        }

        val neighbors = thisTile.adjacent8
        var i = 0
        while (i < 8) {
          val neighbor = neighbors(i)
          i += 1

          if (neighbor.valid
            && With.grids.walkable.get(neighbor)
            && ! tiles(neighbor.i).getVisited
            && costToEnd(thisTile) < costMaximum
            // Verify diagonal walkability
            && (neighbor.x == thisTile.x
              || neighbor.y == thisTile.y
              || With.grids.walkable.get(Tile(neighbor.x, thisTile.y))
              || With.grids.walkable.get(Tile(thisTile.x, neighbor.y)))) {

            val neighborCostFrom = tiles(thisTile.i).getCostFrom + costToTile(thisTile)

            if (neighborCostFrom < tiles(neighbor.i).getCostFrom) {
              val tileState = tiles(neighbor.i)
              tileState.setCameFrom(thisTile)
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
