package Performance.Tasks

import Lifecycle.With

import scala.collection.mutable

abstract class AbstractTask {
  
  private var lastRunFrame    : Int = -1
  private var totalRunCount   : Int = 0
  private var totalSkipCount  : Int = 0
  
  final val runtimeMilliseconds = new mutable.Queue[Long]
  final val runtimesToTrack = 10
  
  var urgency   : Int     = 1
  def skippable : Boolean = true
  
  protected def onRun()
  
  final def framesSinceRunning = Math.max(1, With.frame - lastRunFrame)
  final def totalRuns = totalRunCount
  final def totalSkips = totalSkipCount
  
  final def run() {
    val millisecondsBefore = System.nanoTime() / 1000000
    onRun()
    val millisecondsAfter = System.nanoTime() / 1000000
    recordRunDuration(millisecondsAfter - millisecondsBefore)
    lastRunFrame = With.frame
    totalRunCount += 1
  }
  
  final def skip() {
    totalSkipCount += 1
  }
  
  final def recordRunDuration(millisecondsDuration:Long) {
    runtimeMilliseconds.enqueue(Math.max(0L, millisecondsDuration))
    while (runtimeMilliseconds.size > runtimesToTrack) {
      runtimeMilliseconds.dequeue()
    }
  }
  
  final def runMillisecondsMax:Long = {
    if (runtimeMilliseconds.isEmpty) return 0
    runtimeMilliseconds.max
  }
  
  final def runMillisecondsMean:Long = {
    if (runtimeMilliseconds.isEmpty) return 0
    runtimeMilliseconds.sum / runtimeMilliseconds.size
  }
}
