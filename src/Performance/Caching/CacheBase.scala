package Performance.Caching

import Lifecycle.With

abstract class CacheBase[T](recalculator: () => T) {
  
  private var nextUpdateFrame = 0
  private var lastValue: Option[T] = None
  
  def get: T = {
    if (nextUpdateFrame <= With.frame) {
      lastValue = Some(recalculator.apply())
      nextUpdateFrame = With.frame + nextCacheDelay
    }
    lastValue.get
  }
  
  def invalidate() { lastValue = None }
  
  protected def nextCacheDelay: Int
}
