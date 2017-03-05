package Utilities.Caching

import Startup.With

class Limiter(
  frameLimit: Int,
  action:() => Unit) {
  
  var _lastAction = Int.MinValue
  def act() {
    if (With.game.getFrameCount > _lastAction + frameLimit) {
      action()
      _lastAction = With.game.getFrameCount + _getJitter
    }
  }
  
  def _getJitter:Int = if (frameLimit > 1) CacheRandom.random.nextInt(2) else 0
}
