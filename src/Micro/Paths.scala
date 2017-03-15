package Micro

import Startup.With
import bwapi.TilePosition
import bwta.BWTA

import scala.collection.mutable

class Paths {
  
  //Cache ground distances with a LRU (Least-recently used) cache
  //This is a low number; let's increase it after we make sure _limitCacheSize works
  private val impossiblyLargeDistance = Int.MaxValue / 1000
  private val maxCacheSize = 10000 //Max we'll ever need to cache: 256 * 256 = 65536 so maybe this is silly
  private val distanceCache = new mutable.HashMap[(TilePosition, TilePosition), Int]
  private val distanceAge = new mutable.HashMap[(TilePosition, TilePosition), Int]
  
  def exists(origin:TilePosition, destination: TilePosition):Boolean = {
    groundDistance(origin, destination) < impossiblyLargeDistance
  }
  
  def groundDistance(origin:TilePosition, destination:TilePosition):Int = {
    val request = (origin, destination)
    if ( ! distanceCache.contains(request)) {
      cacheDistance(request)
    }
    distanceAge.put(request, With.frame)
    val result = distanceCache(request)
    limitCacheSize()
    
    if (result < 0) {
      return impossiblyLargeDistance
    }
    
    result
  }
  
  private def cacheDistance(request:(TilePosition, TilePosition)) {
    val distance = BWTA.getGroundDistance(request._1, request._2)
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
