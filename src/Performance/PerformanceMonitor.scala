package Performance

import Lifecycle.{PurpleBWClient, With}
import Performance.Tasks.TimedTask
import Utilities.?
import Utilities.Time.{Forever, Seconds}

import scala.collection.mutable.ArrayBuffer

class PerformanceMonitor {
  private val _tasks = new ArrayBuffer[TimedTask]
  def addTask(task: TimedTask): Unit = { _tasks += task}
  def tasks: Seq[TimedTask] = _tasks

  private val frameTimes    : Array[Long] = Array.fill[Long](Seconds(3)())(1)
  private var frameStartMs  : Long  = 0
  var framesOverShort       : Int   = 0
  var framesOver1000        : Int   = 0
  var framesOver10000       : Int   = 0
  var lastTaskWarningFrame  : Int   = -1
  var gameStartMs           : Long  = 0
  val startMillis           : Long  = systemMillis
  var lastStopwatchMillis   : Long  = startMillis
  var lastStopwatchFrames   : Int   = With.frame

  def startFrame(): Unit = {
    frameStartMs = systemMillis
    if (gameStartMs == 0) gameStartMs = frameStartMs
  }

  def endFrame(): Unit = {
    val frameMs = ?(frameHitBreakpoint, frameMeanMs, frameElapsedMs)
    frameTimes(With.frame % frameTimes.length) = frameMs
    if (With.frame > 0) {
      if (frameMs >= With.configuration.frameLimitMs) framesOverShort += 1
      if (frameMs >= 1000)                            framesOver1000  += 1
      if (frameMs >= 10000)                           framesOver10000 += 1
    }
  }

  // The tradeoff here is between using System.currentTimeMillis and System.nanoTime
  // https://www.geeksforgeeks.org/java-system-nanotime-vs-system-currenttimemillis/
  def systemMillis        : Long    = System.nanoTime / 1000000
  def frameMaxMs          : Long    = frameTimes.max
  def frameMeanMs         : Long    = frameTimes.iterator.map(Math.min(_, 100L)).sum / Math.min(Math.max(1, With.frame), frameTimes.length)
  def frameElapsedMs      : Long    = Math.max(0, systemMillis - frameStartMs)
  def msBeforeTarget      : Long    = ?(With.frame == 0, Forever(), With.configuration.frameTargetMs - ?(frameHitBreakpoint, 0, frameElapsedMs))
  def msBeforeLimit       : Long    = ?(With.frame == 0, Forever(), With.configuration.frameLimitMs  - ?(frameHitBreakpoint, 0, frameElapsedMs))
  def frameHitBreakpoint  : Boolean = With.configuration.detectBreakpoints && frameElapsedMs > 24 * 3
  def frameBrokeTarget    : Boolean = With.frame > 0 && msBeforeTarget  < 0
  def frameBrokeLimit     : Boolean = With.frame > 0 && msBeforeLimit   < 0
  def continueRunning     : Boolean = With.frame == 0 || ! With.configuration.enablePerformancePauses || (msBeforeTarget > 0 && PurpleBWClient.framesBehind() < 1)

  def disqualificationDanger: Boolean = (
    With.configuration.enablePerformancePauses && (
      disqualified
      || framesOverShort >= 160
      || framesOver1000  >= 5
      || framesOver10000 >= 1)
  )
  def disqualified: Boolean = (
    framesOverShort     >= 320
    || framesOver1000   >= 10
    || framesOver10000  >= 1)

  def wallClockDurationMillis: Long = systemMillis  - startMillis
  def stopwatchDurationMillis: Long = systemMillis  - lastStopwatchMillis
  def stopwatchDurationFrames: Int  = With.frame    - lastStopwatchFrames

  def resetStopwatch(): Unit = {
    lastStopwatchMillis = systemMillis
    lastStopwatchFrames = With.frame
  }

  def parseCommand(value: String): Boolean = {
    if ( ! value.startsWith("f")) return false
    val duration  = """\d+""".r.findFirstIn(value).map(_.toInt)
    duration.foreach(With.configuration.frameLimitMs = _)
    duration.nonEmpty
  }
}
