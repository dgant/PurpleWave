package Information.Geography.Pathfinding

import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object GroundPathFinder {
  
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
  
  // Statefulness in a singleton!
  // Be careful what you do with this.
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
  private val tiles = new Array[TileState](maximumMapTiles)
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
