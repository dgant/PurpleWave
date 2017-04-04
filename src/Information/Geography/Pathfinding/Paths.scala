package Information.Geography.Pathfinding

import Lifecycle.With
import bwapi.TilePosition
import bwta.BWTA

import scala.collection.mutable
import Utilities.EnrichPosition._

class Paths {
  
  //Cache ground distances with a LRU (Least-recently used) cache
  private val maxCacheSize = 100000
  private val distanceCache = new mutable.HashMap[(TilePosition, TilePosition), Int]
  private val distanceAge = new mutable.HashMap[(TilePosition, TilePosition), Int]
  
  val impossiblyLargeDistance = Int.MaxValue / 1000
  
  def exists(origin:TilePosition, destination: TilePosition, requireBwta:Boolean = false):Boolean = {
    groundPixels(origin, destination, requireBwta) < impossiblyLargeDistance
  }
  
  def groundPixels(origin:TilePosition, destination:TilePosition, requireBwta:Boolean = false):Int = {
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
  
  private def calculateDistance(request:(TilePosition, TilePosition), requireBwta:Boolean) {
    val distance =
      if (With.configuration.enableFastGroundDistance && With.frame > 0 && ! requireBwta)
        PathFinder.roughGroundDistance(request._1.pixelCenter, request._2.pixelCenter)
      else
        BWTA.getGroundDistance(request._1, request._2)
    
    distanceCache.put(request, distance.toInt)
  }
  
  private def limitCacheSize() {
    if (distanceCache.keys.size > maxCacheSize) {
      val cutoff = With.frame - 24 * 60
      distanceAge.filter(_._2 < cutoff).foreach(pair => {
        distanceCache.remove(pair._1)
        distanceAge.remove(pair._1)
      })
    }
  }
}
