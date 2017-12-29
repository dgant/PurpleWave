package Performance

import Lifecycle.With

class Cache[T](recalculator: () => T) {
  
  private var lastValue: T = _
  private val defaultValue = lastValue
  private var invalidStartingOnThisFrame = 0
  
  @inline
  def apply(): T = {
    if (invalidStartingOnThisFrame <= With.frame) {
      lastValue = recalculator()
      invalidStartingOnThisFrame = With.frame + 1
    }
    lastValue
  }
  
  def invalidate() {
    lastValue = defaultValue
    invalidStartingOnThisFrame = With.frame
  }
}
