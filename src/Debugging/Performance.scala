package Debugging

import Lifecycle.With

class Performance {
  private val framesToTrack = 24 * 3
  private val frameTimes = Array.fill(framesToTrack)(1l)
  
  private var millisecondsBefore = 0l
  private var currentFrameDelay = 24
  private var lastFrameDelayUpdate = 0
  
  def startCounting() {
    millisecondsBefore = System.currentTimeMillis()
  }
  
  def stopCounting() {
    val millisecondDifference = System.currentTimeMillis - millisecondsBefore
    frameTimes(With.frame % framesToTrack) = millisecondDifference
  
    if (With.frame % framesToTrack == 0) {
      if (meanFrameLength > 20 || maxFrameLength > 60) {
        currentFrameDelay += 4
      } else {
        currentFrameDelay -= 1
      }
      currentFrameDelay = Math.max(currentFrameDelay, With.latency.turnSize)
      currentFrameDelay = Math.min(currentFrameDelay, 12)
    }
  }
  
  def maxFrameLength:Long = frameTimes.max
  def meanFrameLength:Long = frameTimes.sum / framesToTrack
  def frameDelay(size:Int):Int = currentFrameDelay
}
