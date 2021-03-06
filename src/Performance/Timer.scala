package Performance

import Lifecycle.With

class Timer(duration: Long = With.performance.msBeforeTarget) {
  private def now = With.performance.systemMillis
  val start: Long  = now
  def elapsed: Long = if (With.performance.hitBreakpointThisFrame) 0 else now - start
  def remaining: Long = remainingUntil(start + duration)
  def remainingUntil(milliseconds: Long): Long =
    if (With.performance.hitBreakpointThisFrame)
      With.configuration.frameTargetMs
    else
      milliseconds - now
  def expired: Boolean = remaining <= 0 && With.configuration.enablePerformancePauses
  def ongoing: Boolean = ! expired
}
