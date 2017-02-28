package Utilities

class CacheForever[T] extends Cache[T] {
  
  override def _cacheHasExpired:Boolean = _cachedValue.isEmpty
  
}
