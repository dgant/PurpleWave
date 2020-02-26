package Performance

import Lifecycle.With

class PerformanceMonitor {
  
  private val framesToTrack = 24 * 3
  private val frameTimes = Array.fill(framesToTrack)(1l)

  val frameLimitShort: Int = 55
  
  private var millisecondsBefore = 0l
  private var lastFrameDelayUpdate = 0
  
  var framesOverShort = 0
  var framesOver1000  = 0
  var framesOver10000 = 0

  def enablePerformancePauses: Boolean = With.configuration.enablePerformancePauses // For disabling performance stops while debugging

  // The tradeoff here is between using System.currentTimeMillis and System.nanoTime
  // https://www.geeksforgeeks.org/java-system-nanotime-vs-system-currenttimemillis/
  def systemMillis: Long = System.nanoTime / 1000000

  def startFrame() {
    millisecondsBefore = systemMillis
  }

  def endFrame() {
    var millisecondDifference = millisecondsSpentThisFrame
    if (With.configuration.debugging && millisecondDifference > With.configuration.debugPauseThreshold) {
      millisecondDifference = meanFrameMilliseconds
    }

    frameTimes(With.frame % framesToTrack) = millisecondDifference
    if (millisecondDifference >= frameLimitShort) framesOverShort += 1
    if (millisecondDifference >= 1000)            framesOver1000  += 1
    if (millisecondDifference >= 10000)           framesOver10000 += 1
  }

  def millisecondsLeftBeforeTarget: Long = {
    Math.max(0, With.configuration.targetFrameDurationMilliseconds - millisecondsSpentThisFrame)
  }

  def millisecondsSpentThisFrame: Long = {
    Math.max(0, systemMillis - millisecondsBefore)
  }

  def continueRunning: Boolean = {
    With.frame == 0 || millisecondsLeftBeforeTarget > 1 || ! enablePerformancePauses
  }

  def violatedTarget: Boolean = {
    With.frame > 0 && millisecondsLeftBeforeTarget <= 0
  }

  def violatedLimit: Boolean = {
    With.frame > 0 && millisecondsSpentThisFrame >= frameLimitShort
  }

  def danger: Boolean = (
    With.configuration.enablePerformancePauses
    && (
      framesOverShort > 160 ||
      framesOver1000  > 5   ||
      framesOver10000 > 1)
  )

  def maxFrameMilliseconds  : Long = frameTimes.max
  def meanFrameMilliseconds : Long = frameTimes.view.map(Math.min(_, 100)).sum / framesToTrack

  def disqualified: Boolean =
    framesOverShort >= 320 ||
    framesOver1000  >= 10
}
