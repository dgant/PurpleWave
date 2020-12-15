package Performance

import Lifecycle.With

class Cache[T](recalculator: () => T, refreshPeriod: Int = 1) {
  private var lastValue: T = _
  private val defaultValue: T = lastValue
  private var nextUpdateFrame: Int = 0
  
  @inline final def apply(): T = {
    if (With.frame >= nextUpdateFrame) {
      nextUpdateFrame = With.frame + refreshPeriod
      lastValue = recalculator()
    }
    lastValue
  }
  
  @inline final def invalidate() {
    lastValue = defaultValue
    nextUpdateFrame = With.frame
  }

  override def toString: String = apply().toString
}
