package Global.Information

import Startup.With
import bwapi.TilePosition
import bwta.BWTA

import scala.collection.mutable

class Paths {
  
  //Cache ground distances with a LRU (Least-recently used) cache
  //This is a low number; let's increase it after we make sure _limitCacheSize works
  val _impossiblyLargeDistance = Int.MaxValue / 1000
  val _maxCacheSize = 10000 //Max we'll ever need to cache: 256 * 256 = 65536 so maybe this is silly
  val _distanceCache = new mutable.HashMap[(TilePosition, TilePosition), Int]
  val _distanceAge = new mutable.HashMap[(TilePosition, TilePosition), Int]
  
  def exists(origin:TilePosition, destination: TilePosition):Boolean = {
    groundDistance(origin, destination) < _impossiblyLargeDistance
  }
  
  def groundDistance(origin:TilePosition, destination:TilePosition):Int = {
    val request = (origin, destination)
    if ( ! _distanceCache.contains(request)) {
      _cacheDistance(request)
    }
    _distanceAge.put(request, With.game.getFrameCount)
    val result = _distanceCache(request)
    _limitCacheSize()
    
    if (result < 0) {
      return _impossiblyLargeDistance
    }
    
    result
  }
  
  def _cacheDistance(request:(TilePosition, TilePosition)) {
    val distance = BWTA.getGroundDistance(request._1, request._2)
    _distanceCache.put(request, distance.toInt)
  }
  
  def _limitCacheSize() {
    if (_distanceCache.keys.size > _maxCacheSize) {
      val cutoff = With.game.getFrameCount - 24 * 60
      _distanceAge.filter(_._2 < cutoff).foreach(pair => {
        _distanceCache.remove(pair._1)
        _distanceAge.remove(pair._1)
      })
    }
  }
}
