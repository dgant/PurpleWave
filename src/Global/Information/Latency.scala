package Global.Information

import Startup.With

class Latency {
  
  val minTurnSize = 3
  var lastRunFrame = 0
  var minRemainingLatencyFrames:Int = With.game.getLatencyFrames
  
  def onFrame() {
    minRemainingLatencyFrames = Math.min(minRemainingLatencyFrames, With.game.getRemainingLatencyFrames)
  }
  
  def shouldRun:Boolean = {
    var shouldWeRun =
      With.game.getFrameCount < 5 * With.game.getLatencyFrames ||
      With.game.getRemainingLatencyFrames == Math.max(minTurnSize, minRemainingLatencyFrames) ||
      With.game.getFrameCount - lastRunFrame > With.game.getLatencyFrames
    if (shouldWeRun) lastRunFrame = With.game.getFrameCount
    shouldWeRun
  }
}
