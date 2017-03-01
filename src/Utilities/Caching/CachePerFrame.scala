package Utilities.Caching

class CachePerFrame[T](ourCalculator:() => T) extends Cache(1, ourCalculator) { }
