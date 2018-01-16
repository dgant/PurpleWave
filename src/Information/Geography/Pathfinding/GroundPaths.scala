package Information.Geography.Pathfinding

import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import bwta.BWTA

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

trait GroundPaths {
  
  //Cache ground distances with a LRU (Least-recently used) cache
  private val maxCacheSize  = 1000000
  private val distanceCache = new mutable.HashMap[(Tile, Tile), Double]
  private val distanceAge   = new mutable.HashMap[(Tile, Tile), Double]
  
  val impossiblyLargeDistance: Double = 32.0 * 32.0 * 256.0 * 256.0 * 100.0
  
  def groundPathExists(origin: Tile, destination: Tile, requireBwta: Boolean = false): Boolean = {
    groundPixelsByTile(origin, destination, requireBwta) < impossiblyLargeDistance
  }
  
  def groundPixels(origin: Pixel, destination: Pixel): Double = {
    
    // Let's first check if we can use air distance. It's cheaper and more accurate.
    // We can "get away" with using air distance if:
    // A. We're in the same zone, or
    // B. We're in adjacent zones with the chokepoint between us
    
    var useAirDistance  = false
    val zoneOrigin      = origin.zone
    val zoneDestination = destination.zone
    
    if (zoneOrigin == zoneDestination) {
      return origin.pixelDistanceFast(destination)
    }
    
    // This approximation -- calculating ground distance at tile resolution -- can potentially bite us.
    // Pun intended on "potentially" -- the risk here is using it for potential fields near a chokepoint
    // before which we're getting pixel-resolution distance and after which we're getting tile-resolution distance
    groundPixelsByTile(origin.tileIncluding, destination.tileIncluding)
  }
  
  def groundPixelsByTile(
    origin: Tile,
    destination: Tile,
    requireBwta: Boolean = false): Double = {

    if (origin.zone == destination.zone) {
      return origin.pixelCenter.pixelDistanceFast(destination.pixelCenter)
    }
    
    val request = (origin, destination)
    if ( ! distanceCache.contains(request)) {
      calculateDistance(request, requireBwta)
    }
    distanceAge.put(request, With.frame)
    val result = distanceCache(request)
    limitCacheSize()
    
    if (result < 0) {
      return impossiblyLargeDistance
    }
    
    result
  }
  
  private def calculateDistance(request: (Tile, Tile), requireBwta: Boolean) {
    val distance =
      if (With.frame > 0 && ! requireBwta)
        With.paths.groundDistanceFast(request._1.pixelCenter, request._2.pixelCenter)
      else
        BWTA.getGroundDistance(request._1.bwapi, request._2.bwapi)
    
    distanceCache.put(request, distance)
  }
  
  private def limitCacheSize() {
    if (distanceCache.keys.size > maxCacheSize) {
      val cutoff = With.frame - 24 * 60
      for(p <- distanceAge) {
        if (p._2 < cutoff) {
          distanceCache.remove(p._1)
          distanceAge.remove(p._1)
        }
      }
    }
  }
  
  ////////////////////////////////////////////////////////////
  // From the old GroundPathFinder -- this can be split out //
  ////////////////////////////////////////////////////////////
  
  def groundDistanceFast(from: Pixel, to: Pixel): Double = {
    
    val fromZone = from.zone
    val toZone = to.zone
    
    if (fromZone == toZone) {
      return from.pixelDistanceFast(to)
    }
    
    if ( ! With.paths.groundPathExists(
      fromZone.centroid,
      toZone.centroid,
      requireBwta = true)) {
      return With.paths.impossiblyLargeDistance
    }
    
    val fromEdgeTiles = fromZone.edges.map(_.centerPixel.tileIncluding)
    val toEdgeTiles   =   toZone.edges.map(_.centerPixel.tileIncluding)
    
    fromEdgeTiles.map(fromEdgeTile =>
      toEdgeTiles.map(toEdgeTile =>
        from.pixelDistanceFast(fromEdgeTile.pixelCenter) +
          to.pixelDistanceFast(  toEdgeTile.pixelCenter) +
          With.paths.groundPixelsByTile(
            fromEdgeTile,
            toEdgeTile,
            requireBwta = true))
        .min)
      .min
  }
  
  private val defaultId = -1
  private val maximumMapTiles = 256 * 256
  private var currentTileStateId = 0
  private class TileState {
    var visitedId         : Long    = defaultId
    var cameFromValue     : Tile    = _
    var cameFromId        : Long    = defaultId
    var distanceFromValue : Int     = _
    var distanceFromId    : Long    = defaultId
    var distanceToValue   : Int     = _
    var distanceToId      : Long    = defaultId
    def setVisited {
      visitedId = currentTileStateId
    }
    def setCameFrom(value: Tile) {
      cameFromValue = value
      cameFromId = currentTileStateId
    }
    def setDistanceFrom(value: Int) {
      distanceFromValue = value
      distanceFromId = currentTileStateId
    }
    def setDistanceTo(value: Int) {
      distanceToValue = value
      distanceToId = currentTileStateId
    }
    def getVisited      : Boolean       =     visitedId       == currentTileStateId
    def getCameFrom     : Option[Tile]  = if (cameFromId      == currentTileStateId)  Some(cameFromValue) else None
    def getDistanceFrom : Int           = if (distanceFromId  == currentTileStateId)  distanceFromValue   else Int.MaxValue
    def getDistanceTo   : Int           = if (distanceToId    == currentTileStateId)  distanceToValue     else Int.MaxValue
  }
  
  private val tiles: Array[TileState] = Array.fill(maximumMapTiles){ new TileState }
  private def incrementTileStateId() {
    if (currentTileStateId == Long.MaxValue) {
      for (i <- tiles.indices) { tiles(i) = new TileState }
      currentTileStateId = defaultId
    }
    currentTileStateId += 1
  }
  
  def manhattanGroundDistanceThroughObstacles(
    start           : Tile,
    end             : Tile,
    obstacles       : Set[Tile],
    maximumDistance : Int)
  : TilePath = {
    
    if ( ! start.valid || ! end.valid) {
      return TilePath(start, end, Int.MaxValue, 0, None)
    }
    
    // I don't want to stop
    // until I reach the top.
    // Baby I'm A*. --Prince
    
    incrementTileStateId()
    var totalVisited = 0
    val horizon      = new mutable.PriorityQueue[Tile]()(Ordering.by(_.tileDistanceManhattan(end)))
    tiles(start.i).setDistanceTo(0)
    tiles(start.i).setDistanceFrom(start.tileDistanceManhattan(end))
    horizon += start
    
    while (horizon.nonEmpty) {
      
      val thisTile = horizon.dequeue()
      
      if ( ! tiles(thisTile.i).getVisited) {
        totalVisited += 1
        tiles(thisTile.i).setVisited
        if (thisTile == end) {
          return TilePath(start, end, tiles(end.i).getDistanceFrom, totalVisited, Some(assemblePath(end)))
        }
        
        val neighbors = thisTile.adjacent4
        var i = 0
        while (i < 4) {
          val neighbor = neighbors(i)
          i += 1
          
          if (neighbor.valid
            && With.grids.walkable.get(neighbor)
            && ! obstacles.contains(neighbor)
            && ! tiles(neighbor.i).getVisited
            && thisTile.tileDistanceManhattan(end) < maximumDistance) {
            
            horizon += neighbor
            
            val neighborDistanceFrom  = tiles(thisTile.i).getDistanceFrom + 1
            
            if (neighborDistanceFrom < tiles(neighbor.i).getDistanceFrom) {
              val tileState = tiles(neighbor.i)
              tileState.setCameFrom(thisTile)
              tileState.setDistanceFrom(neighborDistanceFrom)
              tileState.setDistanceTo(neighborDistanceFrom + neighbor.tileDistanceManhattan(end))
            }
          }
        }
      }
    }
    TilePath(start, end, Int.MaxValue, totalVisited, None)
  }
  
  private def assemblePath(end: Tile): Iterable[Tile] = {
    val path = new ListBuffer[Tile]
    path += end
    var last = end
    while (tiles(last.i).getCameFrom.isDefined) {
      last = tiles(last.i).getCameFrom.get
      path.append(last)
    }
    path
  }
}
