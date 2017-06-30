package Information.Geography.Pathfinding

import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}

import scala.collection.mutable

object PathFinder {
  
  def roughGroundDistance(from: Pixel, to: Pixel): Double = {
    
    val fromZone = from.zone
    val toZone = to.zone
    
    if (fromZone == toZone) {
      return from.pixelDistanceSlow(to)
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
        from.pixelDistanceSlow(fromEdgeTile.pixelCenter) +
          to.pixelDistanceSlow(  toEdgeTile.pixelCenter) +
        With.paths.groundPixels(
          fromEdgeTile,
          toEdgeTile,
          requireBwta = true))
        .min)
      .min
  }
  
  def manhattanGroundDistanceThroughObstacles(
    start          : Tile,
    end            : Tile,
    obstacles      : Set[Tile],
    maximumLength  : Int)
      : Option[Int] = {
    
    // I don't want to stop
    // until I reach the top.
    // Baby I'm A*. --Prince
    
    val visited         = new mutable.HashSet[Tile]
    val horizon         = new mutable.PriorityQueue[Tile]()(Ordering.by(_.tileDistanceManhattan(end)))
    val costToStart     = new mutable.HashMap[Tile, Int] { override def default(key: Tile): Int = Int.MaxValue }
    val costToEnd       = new mutable.HashMap[Tile, Int] { override def default(key: Tile): Int = Int.MaxValue }
    costToEnd(start)    = 0
    costToStart(start)  = start.tileDistanceManhattan(end)
    horizon += start
    while (horizon.nonEmpty) {
      val thisTile = horizon.dequeue()
      visited.add(thisTile)
      if (thisTile == end) return Some(costToStart(end))
      val distanceSquared = thisTile.tileDistanceSquared(end)
      val neighbors = Array(thisTile.left, thisTile.right, thisTile.up, thisTile.down)
      var i = 0
      while(i < 4) {
        val nextTile = neighbors(i)
        i += 1
        if (With.grids.walkable.get(nextTile)
          && ! obstacles.contains(nextTile)
          && ! visited.contains(nextTile)) {
          val nextCostToStart = costToStart(thisTile) + 1
          val nextCostToEnd   = nextCostToStart + nextTile.tileDistanceManhattan(end)
          if (nextCostToEnd < maximumLength) {
            horizon += nextTile
            if (nextCostToStart < costToStart(nextTile)) {
              costToStart(nextTile) = nextCostToStart
              costToEnd(nextTile)   = nextCostToEnd
            }
          }
        }
      }
    }
    None
  }
}
