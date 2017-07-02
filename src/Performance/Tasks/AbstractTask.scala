package Performance.Tasks

import Lifecycle.With

import scala.collection.mutable

abstract class AbstractTask {
  
  private var lastRunFrame        : Int   = -1
  private var totalRunCount       : Int   = 0
  private var totalSkipCount      : Int   = 0
  private var maxMillisecondsEver : Long  = 0
  private var violatedThreshold   : Int   = 0
  private var violatedRules       : Int   = 0
  
  final val runtimeMilliseconds = new mutable.Queue[Long]
  final val runtimesToTrack = 10
  
  var urgency             : Int = 1
  def maxConsecutiveSkips : Int = 48
  def overdue             : Boolean = framesSinceRunning > maxConsecutiveSkips
  
  protected def onRun()
  
  final def framesSinceRunning      : Int     = Math.max(1, With.frame - lastRunFrame)
  final def totalRuns               : Int     = totalRunCount
  final def totalSkips              : Int     = totalSkipCount
  final def totalViolatedThreshold  : Int     = violatedThreshold
  final def totalViolatedRules      : Int     = violatedRules
  final def hasNeverRun             : Boolean = totalRuns == 0
  
  final def run() {
    val nanosToMillis = 1000000
    val millisecondsBefore  = System.nanoTime() / nanosToMillis
    onRun()
    val millisecondsAfter   = System.nanoTime() / nanosToMillis
    recordRunDuration(millisecondsAfter - millisecondsBefore)
    lastRunFrame = With.frame
    totalRunCount += 1
  }
  
  final def skip() {
    totalSkipCount += 1
  }
  
  final def recordRunDuration(millisecondsDuration:Long) {
    if (With.frame > 5) {
      maxMillisecondsEver = Math.max(maxMillisecondsEver, millisecondsDuration)
    }
    runtimeMilliseconds.enqueue(Math.max(0L, millisecondsDuration))
    while (runtimeMilliseconds.size > runtimesToTrack) {
      runtimeMilliseconds.dequeue()
    }
    if (With.performance.violatedThreshold) {
      violatedThreshold += 1
    }
    if (With.performance.violatedRules) {
      violatedRules += 1
    }
  }
  
  final def runMillisecondsMaxRecent: Long = {
    if (runtimeMilliseconds.isEmpty) return 0
    runtimeMilliseconds.max
  }
  
  final def runMillisecondsMaxAllTime: Long = {
    maxMillisecondsEver
  }
  
  final def runMillisecondsMean: Long = {
    if (runtimeMilliseconds.isEmpty) return 0
    runtimeMilliseconds.sum / runtimeMilliseconds.size
  }
}
