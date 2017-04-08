package Performance.Caching

import Lifecycle.With

abstract class CacheBase[T](recalculator:() => T) {
  
  private var nextUpdateFrame = 0
  var lastValue:Option[T] = None
  
  def get:T = {
    if (nextUpdateFrame <= With.frame) {
      lastValue = Some(recalculator.apply())
      nextUpdateFrame = With.frame + nextCacheDelay
    }
    lastValue.get
  }
  
  protected def nextCacheDelay:Int
}
