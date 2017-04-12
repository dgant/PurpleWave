package Performance.Systems

import Lifecycle.With

import scala.collection.mutable

abstract class AbstractSystem {
  
  private var lastRunFrame:Int = -1
  
  final val runtimeMilliseconds = new mutable.Queue[Long]
  final val runtimesToTrack = 30
  
  def urgency:Int
  protected def onRun()
  
  final def framesSinceRunning = With.frame - lastRunFrame
  
  final def run {
    lastRunFrame = With.frame
    val millisecondsBefore = System.currentTimeMillis()
    onRun()
    val millisecondsAfter = System.currentTimeMillis()
    recordRunDuration(millisecondsAfter - millisecondsBefore)
  }
  
  final def recordRunDuration(millisecondsDuration:Long) {
    runtimeMilliseconds.enqueue(millisecondsDuration)
    while (runtimeMilliseconds.size > runtimesToTrack) {
      runtimeMilliseconds.dequeue()
    }
  }
  
  final def maxRunMilliseconds:Long = {
    if (runtimeMilliseconds.isEmpty) return 0
    runtimeMilliseconds.max
  }
  
  final def averageRunMilliseconds:Long = {
    if (runtimeMilliseconds.isEmpty) return 0
    runtimeMilliseconds.sum / runtimeMilliseconds.size
  }
}
