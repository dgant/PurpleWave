package Performance.Caching

import Lifecycle.With

class Cache[T](
  frameDelayScale:Int,
  recalculator:() => T)
  extends CacheBase[T](recalculator) {
  
  protected def nextCacheDelay:Int = With.performance.frameDelay(frameDelayScale) + jitter
  private def jitter:Int = CacheRandom.random.nextInt(2)
}
