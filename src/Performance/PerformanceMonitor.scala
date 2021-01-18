package Performance

import Lifecycle.{JBWAPIClient, With}
import Performance.Tasks.TimedTask
import Utilities.Forever

import scala.collection.mutable.ArrayBuffer

class PerformanceMonitor {
  
  private val framesToTrack = 24 * 3
  private val frameTimes = Array.fill(framesToTrack)(1l)

  private var frameStartMs = 0l
  private var lastFrameDelayUpdate = 0

  var framesOverShort = 0
  var framesOver1000  = 0
  var framesOver10000 = 0

  // The tradeoff here is between using System.currentTimeMillis and System.nanoTime
  // https://www.geeksforgeeks.org/java-system-nanotime-vs-system-currenttimemillis/
  def systemMillis: Long = System.nanoTime / 1000000

  def startFrame() {
    frameStartMs = systemMillis
  }

  def endFrame() {
    var violated = violatedTarget
    val thisFrameMs = if (hitBreakpointThisFrame) frameMeanMs else frameMs

    frameTimes(With.frame % framesToTrack) = frameMs
    if (thisFrameMs >= With.configuration.frameLimitMs) framesOverShort += 1
    if (thisFrameMs >= 1000)                            framesOver1000  += 1
    if (thisFrameMs >= 10000)                           framesOver10000 += 1
  }

  def hitBreakpointThisFrame: Boolean = {
    With.configuration.detectBreakpoints && frameMs > 24 * 3
  }

  def msBeforeTarget: Long = {
    if (With.frame == 0)
      Forever()
    else
      With.configuration.frameTargetMs - (if (hitBreakpointThisFrame) 0 else frameMs)
  }

  def frameMs: Long = Math.max(0, systemMillis - frameStartMs)

  def continueRunning: Boolean = (
    With.frame == 0
    || ! With.configuration.enablePerformancePauses
    || (msBeforeTarget > 1 && JBWAPIClient.framesBehind() < 1))

  def violatedTarget  : Boolean = With.frame > 0 && msBeforeTarget <= 0
  def violatedLimit   : Boolean = With.frame > 0 && frameMs >= With.configuration.frameLimitMs
  def danger: Boolean = (
    With.configuration.enablePerformancePauses
    && (
      framesOverShort > 160 ||
      framesOver1000  > 5   ||
      framesOver10000 > 1)
  )

  def frameMaxMs: Long = frameTimes.max
  def frameMeanMs : Long = frameTimes.view.map(Math.min(_, 100)).sum / framesToTrack

  def disqualified: Boolean = framesOverShort >= 320 || framesOver1000 >= 10

  private val _tasks = new ArrayBuffer[TimedTask]
  def addTask(task: TimedTask): Unit = { _tasks += task}
  def tasks: Seq[TimedTask] = _tasks
}
