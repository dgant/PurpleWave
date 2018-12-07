package Performance

import Lifecycle.With

class Cache[T](recalculator: () => T, refreshPeriod: Int = 1) {
  
  private var lastValue: T = _
  private val defaultValue: T = lastValue
  private var invalidStartingOnThisFrame: Int = 0
  
  @inline
  def apply(): T = {
    if (invalidStartingOnThisFrame <= With.frame) {
      invalidStartingOnThisFrame = With.frame + refreshPeriod
      lastValue = recalculator()
    }
    lastValue
  }
  
  def invalidate() {
    lastValue = defaultValue
    invalidStartingOnThisFrame = With.frame
  }

  override def toString: String = apply().toString
}
