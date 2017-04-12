package Performance.Caching

import Lifecycle.With

class Cache[T](
  frameDelayScale:Int,
  recalculator:() => T)
  extends CacheBase[T](recalculator) {
  
  protected def nextCacheDelay:Int = With.performance.cacheLength(frameDelayScale) + CacheRandom.random.nextInt(2)
}
