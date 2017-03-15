package Performance

import Startup.With

class Latency {
  
  val minTurnSize = 2
  var lastRunFrame = 0
  var minRemainingLatencyFrames:Int = With.game.getLatencyFrames
  
  def onFrame() {
    minRemainingLatencyFrames = Math.min(minRemainingLatencyFrames, With.game.getRemainingLatencyFrames)
  }
  
  def shouldRun:Boolean = {
    var shouldWeRun =
      With.frame < 5 * With.game.getLatencyFrames ||
      With.game.getRemainingLatencyFrames == Math.max(minTurnSize, minRemainingLatencyFrames) ||
      With.frame - lastRunFrame > With.game.getLatencyFrames || With.game.isPaused
    if (shouldWeRun) lastRunFrame = With.frame
    shouldWeRun
  }
}
