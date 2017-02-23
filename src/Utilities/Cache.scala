package Utilities

import Startup.With

class Cache[T]() {
  
  var duration = 24
  var _lastUpdateFrame = Integer.MIN_VALUE
  var _cachedValue:Option[T] = None
  
  var _recalculator:Option[() => T] = None
  
  def setCalculator(recalculator:() => T) {
    _recalculator = Some(recalculator)
  }
  
  def get:T = {
    if (_cacheHasExpired) {
      _cachedValue = Some(_recalculateAsNeeded)
      _lastUpdateFrame = With.game.getFrameCount
    }
    _cachedValue.get
  }
  
  def _recalculateAsNeeded:T = {
    if (_recalculator.isEmpty) {
      throw new Exception("This cache lacks a recalculator")
    }
    _recalculator.get.apply()
  }
  
  def _cacheHasExpired:Boolean = {
    _lastUpdateFrame < 0 || With.game.getFrameCount - _lastUpdateFrame > duration
  }
}
