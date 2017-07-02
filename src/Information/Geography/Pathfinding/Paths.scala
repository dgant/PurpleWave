package Information.Geography.Pathfinding

import Lifecycle.With
import Mathematics.Points.Tile
import bwta.BWTA

import scala.collection.mutable

class Paths {
  
  //Cache ground distances with a LRU (Least-recently used) cache
  private val maxCacheSize = 100000
  private val distanceCache = new mutable.HashMap[(Tile, Tile), Double]
  private val distanceAge = new mutable.HashMap[(Tile, Tile), Double]
  
  val impossiblyLargeDistance: Double = 32.0 * 32.0 * 256.0 * 256.0 * 100.0
  
  def exists(origin:Tile, destination: Tile, requireBwta:Boolean = false):Boolean = {
    groundPixels(origin, destination, requireBwta) < impossiblyLargeDistance
  }
  
  def groundPixels(origin: Tile, destination: Tile, requireBwta: Boolean = false):Double = {
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
      if (With.configuration.useFastGroundDistance && With.frame > 0 && ! requireBwta)
        PathFinder.groundDistanceFast(request._1.pixelCenter, request._2.pixelCenter)
      else
        BWTA.getGroundDistance(request._1.bwapi, request._2.bwapi)
    
    distanceCache.put(request, distance)
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
