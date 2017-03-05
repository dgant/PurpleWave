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
      _lastUpdateFrame = With.game.getFrameCount
    }
    _cachedValue.get
  }
  
  def _recalculateAsNeeded:T = recalculator.apply()
  def _cacheHasExpired:Boolean = _lastUpdateFrame < 0 || With.game.getFrameCount - _lastUpdateFrame > duration
}
