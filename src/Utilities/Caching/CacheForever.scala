package Utilities.Caching

class CacheForever[T](ourCalculator:() => T) extends CacheBase[T](Int.MaxValue, ourCalculator) {
  
  override def _cacheHasExpired:Boolean = _cachedValue.isEmpty
  
}
