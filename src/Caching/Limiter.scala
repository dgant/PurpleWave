package Caching

import Startup.With

class Limiter(
  frameLimit: Int,
  action:() => Unit) {
  
  var _lastAction = Int.MinValue
  def act() {
    if (With.game.getFrameCount > _lastAction + frameLimit) {
      action()
      _lastAction = With.game.getFrameCount
    }
  }
}
