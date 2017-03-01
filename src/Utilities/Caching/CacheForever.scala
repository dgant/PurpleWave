package Utilities.Caching

class CacheForever[T](ourCalculator:() => T) extends Cache[T](Int.MaxValue, ourCalculator) {
  
  override def _cacheHasExpired:Boolean = _cachedValue.isEmpty
  
}
