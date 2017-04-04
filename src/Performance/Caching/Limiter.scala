package Performance.Caching

import Lifecycle.With

class Limiter(frameDelayScale:Int, action:() => Unit) extends LimiterBase(action) {
  protected def frameDelay:Int = With.performance.frameDelay(frameDelayScale) + jitter
  private def jitter:Int = CacheRandom.random.nextInt(2)
}
