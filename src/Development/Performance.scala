package Development

import Startup.With

class Performance {
  private val framesToTrack = 24 * 10
  private val frameTimes = Array.fill(framesToTrack)(1l)
  private val framesPerPerformanceTweak = 24
  
  private var millisecondsBefore = 0l
  private var currentFrameDelay = 12
  private var lastFrameDelayUpdate = 0
  
  def startCounting() {
    millisecondsBefore = System.currentTimeMillis()
  }
  
  def stopCounting() {
    val millisecondDifference = System.currentTimeMillis - millisecondsBefore
    frameTimes(With.game.getFrameCount % framesToTrack) = millisecondDifference
  
    if (With.game.getFrameCount % framesPerPerformanceTweak == 0) {
      if (meanFrameLength > 15 || maxFrameLength > 50) {
        currentFrameDelay += 1
      }  else {
        currentFrameDelay -= 1
      }
      currentFrameDelay = Math.max(12,  currentFrameDelay)
      currentFrameDelay = Math.min(96, currentFrameDelay)
    }
  }
  
  def maxFrameLength:Long = frameTimes.max
  def meanFrameLength:Long = frameTimes.sum / framesToTrack
  def frameDelay(size:Int):Int = currentFrameDelay
}
