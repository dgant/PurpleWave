package Performance.Tasks

import Lifecycle.With
import Performance.Cache
import Utilities.ByOption

import scala.collection.mutable

abstract class AbstractTask {
  
  private var lastRunFrame          : Int   = -1
  private var totalRunCount         : Int   = 0
  private var totalSkipCount        : Int   = 0
  private var totalMillisecondsEver : Long  = 0
  private var maxMillisecondsEver   : Long  = 0
  private var violatedThreshold     : Int   = 0
  private var violatedRules         : Int   = 0
  
  final val runtimeMilliseconds = new mutable.Queue[Long]
  final val runtimesToTrack = 10
  
  var urgency             : Int = 1
  def maxConsecutiveSkips : Int = 48
  def due                 : Boolean = framesSinceRunning > maxConsecutiveSkips
  
  private var alreadyViolatedTarget  = false
  private var alreadyViolatedLimit   = false
  
  protected def onRun()
  def onEnd() {}
  
  final def framesSinceRunning      : Int     = Math.max(1, With.framesSince(lastRunFrame))
  final def totalRuns               : Int     = totalRunCount
  final def totalSkips              : Int     = totalSkipCount
  final def totalViolatedThreshold  : Int     = violatedThreshold
  final def totalViolatedRules      : Int     = violatedRules
  final def hasNeverRun             : Boolean = totalRuns == 0
  
  private val nanosToMillis = 1000000
  final def run() {
    
    val millisecondsBefore  = System.nanoTime() / nanosToMillis
    alreadyViolatedTarget  = With.performance.violatedTarget
    alreadyViolatedLimit      = With.performance.violatedLimit
    onRun()
    val millisecondsAfter   = System.nanoTime() / nanosToMillis
    var millisecondsDelta   = millisecondsAfter - millisecondsBefore

    // Debugging (ie. setting breakpoints) terribly breaks performance monitoring;
    // so we detect debug pauses and ignore them
    if (With.configuration.debugging && millisecondsDelta >= With.configuration.debugPauseThreshold) {
      millisecondsDelta = runMillisecondsMean
    }

    recordRunDuration(millisecondsDelta)
    lastRunFrame = With.frame
    totalRunCount += 1
  }
  
  final def skip() {
    totalSkipCount += 1
  }
  
  final def recordRunDuration(millisecondsDuration: Long) {
    totalMillisecondsEver += millisecondsDuration
    if (With.frame > 5) {
      maxMillisecondsEver = Math.max(maxMillisecondsEver, millisecondsDuration)
    }
    runtimeMilliseconds.enqueue(Math.max(0L, millisecondsDuration))
    while (runtimeMilliseconds.size > runtimesToTrack) {
      runtimeMilliseconds.dequeue()
    }
    if ( ! alreadyViolatedTarget && With.performance.violatedTarget) {
      violatedThreshold += 1
    }
    if ( ! alreadyViolatedLimit && With.performance.violatedLimit) {
      violatedRules += 1
      if (maxConsecutiveSkips > 1) {
        With.logger.warn(toString + " went too long: " + millisecondsDuration + "ms for " + With.performance.millisecondsSpentThisFrame + "ms this frame. " + (if (due) "It was due to run" else ""))
      }
    }
  }
  
  final def runMillisecondsTotal: Long = {
    totalMillisecondsEver
  }
  
  final val runMillisecondsMaxRecent = new Cache[Long](() => ByOption.max(runtimeMilliseconds).getOrElse(0))
  protected final val runMillisecondsSumRecent = new Cache(() => runtimeMilliseconds.view.map(Math.min(_, 100)).sum)
  
  final def runMillisecondsMaxAllTime: Long = {
    maxMillisecondsEver
  }
  
  final def runMillisecondsMean: Long = runMillisecondsSumRecent() / Math.max(1, runtimeMilliseconds.size)

  override val toString: String = if (getClass.getSimpleName.contains("anon")) getClass.getSuperclass.getSimpleName else getClass.getSimpleName
}
