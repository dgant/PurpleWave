package Performance

import Lifecycle.With

class Timer(duration: Long = With.performance.msBeforeTarget) {
  val start: Long = With.performance.systemMillis
  def elapsed: Long = if (With.performance.hitBreakpointThisFrame) 0 else With.performance.systemMillis - start
  def remaining: Long = remainingUntil(start + duration)
  def remainingUntil(milliseconds: Long): Long = milliseconds - elapsed
  def expired: Boolean = remaining < 0
  def ongoing: Boolean = ! expired
}
