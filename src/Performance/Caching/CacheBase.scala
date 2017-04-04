package Performance.Caching

import Lifecycle.With

abstract class CacheBase[T](recalculator:() => T) {
  
  private var nextUpdateFrame = 0
  var lastValue:Option[T] = None
  
  def get:T = {
    if (cacheHasExpired) {
      lastValue = Some(recalculateAsNeeded)
      nextUpdateFrame = With.frame + nextCacheDelay
    }
    lastValue.get
  }
  
  private def cacheHasExpired:Boolean = nextUpdateFrame <= With.frame
  private def recalculateAsNeeded:T = recalculator.apply()
  
  protected def nextCacheDelay:Int
}
