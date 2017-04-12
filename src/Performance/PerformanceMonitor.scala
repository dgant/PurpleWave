package Performance

import Lifecycle.With

class PerformanceMonitor {
  
  private val framesToTrack = 24 * 3
  private val frameTimes = Array.fill(framesToTrack)(1l)
  
  private var millisecondsBefore = 0l
  private var currentCacheLength = 24
  private var lastFrameDelayUpdate = 0
  
  def startFrame() {
    millisecondsBefore = System.currentTimeMillis()
  }
  
  def endFrame() {
    val millisecondDifference = millisecondsThisFrame
    frameTimes(With.frame % framesToTrack) = millisecondDifference
    updateFrameDelay()
  }
  
  private def updateFrameDelay() = {
    if (With.frame % framesToTrack == 0) {
      if (meanFrameMilliseconds > 20 || maxFrameMilliseconds > 60) {
        currentCacheLength += 4
      } else {
        currentCacheLength -= 1
      }
      currentCacheLength = Math.max(currentCacheLength, With.latency.turnSize)
      currentCacheLength = Math.min(currentCacheLength, 12)
    }
  }
  
  def millisecondsLeftThisFrame = Math.max(0, With.configuration.maxFrameMilliseconds - millisecondsThisFrame)
  def millisecondsThisFrame     = Math.max(0, System.currentTimeMillis - millisecondsBefore)
  
  def maxFrameMilliseconds  : Long = frameTimes.max
  def meanFrameMilliseconds : Long = frameTimes.sum / framesToTrack
  
  def cacheLength(size:Int):Int = currentCacheLength
}
