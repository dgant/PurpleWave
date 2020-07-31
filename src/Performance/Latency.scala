package Performance

import Lifecycle.With

class Latency {
  
  var lastRunFrame = 0
  var latencyFrames: Int = With.game.getLatencyFrames
  var minRemainingLatencyFrames: Int = latencyFrames
  
  def onFrame() {
    minRemainingLatencyFrames = Math.min(minRemainingLatencyFrames, With.game.getRemainingLatencyFrames)
  }
  
  def framesRemaining: Int = remainingFramesCache()
  private val remainingFramesCache = new Cache(() => With.game.getRemainingLatencyFrames)
  
  def turnSize: Int = {
    if (lastRunFrame > 0) {
      if (latencyFrames <= 3)
        return 1
      else if (latencyFrames == 6)
        return 2
    }
    //This doesn't accurately track changes to latency settings during the game.
    1 + Math.max(0, With.game.getLatencyFrames - minRemainingLatencyFrames)
  }
  
  def isLastFrameOfTurn: Boolean = {
    val shouldWeRun =
      With.frame == 0 ||
      With.game.getRemainingLatencyFrames == minRemainingLatencyFrames ||
      With.framesSince(lastRunFrame) > minRemainingLatencyFrames ||
      With.game.isPaused
    if (shouldWeRun) lastRunFrame = With.frame
    shouldWeRun
  }
}
