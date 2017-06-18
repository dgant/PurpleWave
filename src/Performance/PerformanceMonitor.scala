package Performance

import Lifecycle.With

class PerformanceMonitor {
  
  private val framesToTrack = 24 * 3
  private val frameTimes = Array.fill(framesToTrack)(1l)
  
  private var millisecondsBefore = 0l
  private var currentCacheLength = 3
  private var lastFrameDelayUpdate = 0
  
  var framesOver55    = 0
  var framesOver1000  = 0
  var framesOver10000 = 0
  
  def startFrame() {
    millisecondsBefore = System.currentTimeMillis()
  }
  
  def endFrame() {
    val millisecondDifference = millisecondsSpentThisFrame
    frameTimes(With.frame % framesToTrack) = millisecondDifference
    updateFrameDelay()
    if (millisecondDifference >= 55)    framesOver55    += 1
    if (millisecondDifference >= 1000)  framesOver1000  += 1
    if (millisecondDifference >= 10000) framesOver10000 += 1
  }
  
  private def updateFrameDelay() = {
    if (With.frame % framesToTrack == 0) {
      if (meanFrameMilliseconds > 20 || maxFrameMilliseconds > 60) {
        currentCacheLength += 4
      } else {
        currentCacheLength -= 1
      }
      currentCacheLength = Math.max(currentCacheLength, With.latency.turnSize)
      currentCacheLength = Math.min(currentCacheLength, 4)
    }
  }
  
  def millisecondsLeftThisFrame: Long = {
    Math.max(0, With.configuration.peformanceFrameMilliseconds - millisecondsSpentThisFrame)
  }
  
  def millisecondsSpentThisFrame: Long = {
    Math.max(0, System.currentTimeMillis - millisecondsBefore)
  }
  
  def continueRunning:Boolean = {
    millisecondsLeftThisFrame > 1
  }
  
  def overrunning:Boolean = {
    millisecondsSpentThisFrame >= 50
  }
  
  def danger:Boolean = {
    framesOver55    > 100 ||
    framesOver1000  > 5   ||
    framesOver10000 > 1
  }
  
  def maxFrameMilliseconds  : Long = frameTimes.max
  def meanFrameMilliseconds : Long = frameTimes.sum / framesToTrack
  
  def cacheLength(size:Int):Int = currentCacheLength
  
  def disqualified: Boolean =
    framesOver55    >= 200  ||
    framesOver1000  >= 10   ||
    framesOver10000 >= 2
}
