package Performance

import Lifecycle.With
import Performance.Caching.CacheFrame

class Latency {
  
  var lastRunFrame = 0
  var latencyFrames: Int   = With.game.getLatencyFrames
  var minRemainingLatencyFrames: Int = latencyFrames
  
  def onFrame() {
    minRemainingLatencyFrames = Math.min(minRemainingLatencyFrames, With.game.getRemainingLatencyFrames)
  }
  
  def framesRemaining:Int = remainingFramesCache.get
  private val remainingFramesCache = new CacheFrame(() => With.game.getRemainingLatencyFrames)
  
  def turnSize:Int = {
    //This doesn't accurately track changes to latency settings during the game.
    1 + Math.max(0, With.game.getLatencyFrames - minRemainingLatencyFrames)
  }
  
  def isLastFrameOfTurn:Boolean = {
    val shouldWeRun =
      With.frame == 0 ||
      With.game.getRemainingLatencyFrames == minRemainingLatencyFrames ||
      With.framesSince(lastRunFrame) > minRemainingLatencyFrames ||
      With.game.isPaused
    if (shouldWeRun) lastRunFrame = With.frame
    shouldWeRun
  }
}
