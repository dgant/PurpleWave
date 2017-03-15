package Debugging

import Startup.With

class Performance {
  private val framesToTrack = 24 * 6
  private val frameTimes = Array.fill(framesToTrack)(1l)
  
  private var millisecondsBefore = 0l
  private var currentFrameDelay = 36
  private var lastFrameDelayUpdate = 0
  
  def startCounting() {
    millisecondsBefore = System.currentTimeMillis()
  }
  
  def stopCounting() {
    val millisecondDifference = System.currentTimeMillis - millisecondsBefore
    frameTimes(With.frame % framesToTrack) = millisecondDifference
  
    if (With.frame % framesToTrack == 0) {
      if (meanFrameLength > 20 || maxFrameLength > 80) {
        currentFrameDelay += 1
      } else {
        currentFrameDelay -= 1
      }
      currentFrameDelay = Math.max(6,  currentFrameDelay)
      currentFrameDelay = Math.min(72, currentFrameDelay)
    }
  }
  
  def maxFrameLength:Long = frameTimes.max
  def meanFrameLength:Long = frameTimes.sum / framesToTrack
  def frameDelay(size:Int):Int = currentFrameDelay
}
