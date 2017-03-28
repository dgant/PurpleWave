package Debugging

import Startup.With

class Performance {
  private val framesToTrack = 24 * 1
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
      if (meanFrameLength > 20 || maxFrameLength > 80) {
        currentFrameDelay += 2
      } else {
        currentFrameDelay -= 2
      }
      currentFrameDelay = Math.max(6,  currentFrameDelay)
      currentFrameDelay = Math.min(48, currentFrameDelay)
    }
  }
  
  def maxFrameLength:Long = frameTimes.max
  def meanFrameLength:Long = frameTimes.sum / framesToTrack
  def frameDelay(size:Int):Int = currentFrameDelay
}
