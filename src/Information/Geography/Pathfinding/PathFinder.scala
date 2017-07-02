package Information.Geography.Pathfinding

import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object PathFinder {
  
  def groundDistanceFast(from: Pixel, to: Pixel): Double = {
    
    val fromZone = from.zone
    val toZone = to.zone
    
    if (fromZone == toZone) {
      return from.pixelDistanceFast(to)
    }
    
    if ( ! With.paths.exists(
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
        With.paths.groundPixels(
          fromEdgeTile,
          toEdgeTile,
          requireBwta = true))
        .min)
      .min
  }
  
  case class TilePath(
    start     : Tile,
    end       : Tile,
    distance  : Int,
    visited   : Int,
    tiles     : Option[Iterable[Tile]]) {
    def pathExists: Boolean = tiles.nonEmpty
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
    
    val visited         = new mutable.HashSet[Tile]
    val horizon         = new mutable.PriorityQueue[Tile]()(Ordering.by(_.tileDistanceManhattan(end)))
    val cameFrom        = new mutable.HashMap[Tile, Tile]
    val distanceFrom    = new mutable.HashMap[Tile, Int] { override def default(key: Tile): Int = Int.MaxValue }
    val distanceTo      = new mutable.HashMap[Tile, Int] { override def default(key: Tile): Int = Int.MaxValue }
    distanceTo(start)    = 0
    distanceFrom(start)  = start.tileDistanceManhattan(end)
    horizon += start
    
    while (horizon.nonEmpty) {
      
      val thisTile = horizon.dequeue()
      
      if ( ! visited.contains(thisTile)) {
        
        visited.add(thisTile)
        if (thisTile == end) {
          return TilePath(start, end, distanceFrom(end), visited.size, Some(assemblePath(cameFrom, end)))
        }
        
        val neighbors = Array(thisTile.left, thisTile.right, thisTile.up, thisTile.down)
        var i = 0
        while (i < 4) {
          val neighbor = neighbors(i)
          i += 1
          
          if (neighbor.valid
            && With.grids.walkable.get(neighbor)
            && ! obstacles.contains(neighbor)
            && ! visited.contains(neighbor)
            && thisTile.tileDistanceManhattan(end) < maximumDistance) {
  
            horizon += neighbor
            
            val neighborDistanceFrom  = distanceFrom(thisTile) + 1
            
            if (neighborDistanceFrom < distanceFrom(neighbor)) {
              cameFrom(neighbor)      = thisTile
              distanceFrom(neighbor)  = neighborDistanceFrom
              distanceTo(neighbor)    = neighborDistanceFrom + neighbor.tileDistanceManhattan(end)
            }
          }
        }
      }
    }
    TilePath(start, end, Int.MaxValue, visited.size, None)
  }
  
  private def assemblePath(cameFrom: mutable.Map[Tile, Tile], end: Tile): Iterable[Tile] = {
    val path = new ListBuffer[Tile]
    path += end
    var last = end
    while (cameFrom.contains(last)) {
      last = cameFrom(last)
      path.append(last)
    }
    path
  }
}
