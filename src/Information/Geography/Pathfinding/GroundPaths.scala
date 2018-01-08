package Information.Geography.Pathfinding

import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import bwta.BWTA

import scala.collection.mutable

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
        GroundPathFinder.groundDistanceFast(request._1.pixelCenter, request._2.pixelCenter)
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
}
