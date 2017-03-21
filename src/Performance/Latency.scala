package Performance

import Startup.With

class Latency {
  
  var lastRunFrame = 0
  var minRemainingLatencyFrames:Int = With.game.getLatencyFrames
  
  def onFrame() {
    minRemainingLatencyFrames = Math.min(minRemainingLatencyFrames, With.game.getRemainingLatencyFrames)
  }
  
  def turnSize:Int = {
    //This doesn't accurately track changes to latency settings during the game.
    1 + Math.max(0, With.game.getLatencyFrames - minRemainingLatencyFrames)
  }
  
  def shouldRun:Boolean = {
    val shouldWeRun =
      With.frame == 0 ||
      With.game.getRemainingLatencyFrames == minRemainingLatencyFrames ||
      With.frame - lastRunFrame > minRemainingLatencyFrames ||
      With.game.isPaused
    if (shouldWeRun) lastRunFrame = With.frame
    shouldWeRun
  }
}
