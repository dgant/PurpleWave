package Performance.Caching

class CacheForever[T](ourCalculator:() => T) extends CacheBase(ourCalculator) {
  def nextCacheDelay = 24 * 60 * 60 * 24
}

