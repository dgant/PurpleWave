package Performance.Caching

class CacheFrame[T](ourCalculator:() => T) extends CacheBase(ourCalculator) {
  def nextCacheDelay = 1
}
