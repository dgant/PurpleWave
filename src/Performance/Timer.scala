package Performance

import Lifecycle.With

class Timer(duration: Long = With.performance.msBeforeTarget) {
  private def now = With.performance.systemMillis
  val start: Long  = now
  def elapsed: Long = if (With.performance.frameHitBreakpoint) 0 else now - start
  def remaining: Long = remainingUntil(start + duration)
  def remainingUntil(milliseconds: Long): Long =
    if (With.performance.frameHitBreakpoint)
      With.configuration.frameTargetMs
    else
      milliseconds - now
  def redLight    : Boolean = remaining <= 0 && With.configuration.enablePerformancePauses
  def greenLight  : Boolean = ! redLight
}
