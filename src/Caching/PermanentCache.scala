package Caching

class PermanentCache[T] extends Cache[T] {
  
  override def _cacheHasExpired:Boolean = _cachedValue.isEmpty
  
}
