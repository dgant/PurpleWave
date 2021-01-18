package Performance

import Lifecycle.With

class Timer {
  val start: Long = With.performance.systemMillis
  def elapsed: Long = {
    val output = With.performance.systemMillis - start
    if (With.performance.hitBreakpointThisFrame) {
      return 0
    }
    output
  }
  def remainingUntil(milliseconds: Long): Long = milliseconds - elapsed
}
