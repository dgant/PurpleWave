package Caching

import Startup.With

class Cache[T]() {
  
  var duration = 24
  var _lastUpdateFrame = Integer.MIN_VALUE
  var _cachedValue:Option[T] = None
  
  def recalculate: T = {
    throw new Exception("This cache doesn't know how to recalculate!")
  }
  
  def get:T = {
    if (_cacheHasExpired) {
      _cachedValue = Some(recalculate)
      _lastUpdateFrame = With.game.getFrameCount
    }
    _cachedValue.get
  }
  
  def _cacheHasExpired:Boolean = {
    _lastUpdateFrame < 0 || With.game.getFrameCount - _lastUpdateFrame > duration
  }
}
