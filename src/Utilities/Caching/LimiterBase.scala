package Utilities.Caching

import Startup.With

abstract class LimiterBase(action:() => Unit) {
  
  private var nextAction = 0
  
  def act() {
    if (With.game.getFrameCount >= nextAction) {
      action()
      nextAction = With.game.getFrameCount + frameDelay
    }
  }
  
  protected def frameDelay:Int
}
