package Performance

import Lifecycle.With

class Cache[T](recalculator: () => T) {
  
  private var invalidStartingOnThisFrame = 0
  private var lastValue: Option[T] = None
  
  def apply(): T = {
    if (invalidStartingOnThisFrame <= With.frame) {
      lastValue = Some(recalculator.apply())
      invalidStartingOnThisFrame = With.frame + 1
    }
    lastValue.get
  }
  
  def invalidate() {
    lastValue = None
    invalidStartingOnThisFrame = With.frame
  }
}
