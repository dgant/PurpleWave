package Utilities.Caching

import Startup.With

class Cache[T](
  val duration:Int,
  val recalculator:() => T) {
  
  var _lastUpdateFrame = Integer.MIN_VALUE
  var _cachedValue:Option[T] = None
  
  def get:T = {
    if (_cacheHasExpired) {
      _cachedValue = Some(_recalculateAsNeeded)
      _lastUpdateFrame = With.game.getFrameCount + _getJitter
    }
    _cachedValue.get
  }
  
  def _getJitter:Int = if (duration > 1) CacheRandom.random.nextInt(2) else 0
  def _recalculateAsNeeded:T = recalculator.apply()
  def _cacheHasExpired:Boolean = _lastUpdateFrame < 0 || With.game.getFrameCount - _lastUpdateFrame > duration
}
