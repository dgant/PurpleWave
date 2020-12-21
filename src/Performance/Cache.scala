package Performance

import Lifecycle.With

class Cache[T](getValue: () => T, refreshPeriod: Int = 1) {
  private var nextUpdateFrame: Int = 0
  private var lastValue: T = _
  private val defaultValue: T = lastValue

  @inline final def apply(): T = {
    if (With.frame >= nextUpdateFrame) {
      nextUpdateFrame = With.frame + refreshPeriod
      lastValue = getValue()
    }
    lastValue
  }
  
  @inline final def invalidate() {
    nextUpdateFrame = With.frame
    lastValue = defaultValue
  }

  override def toString: String = apply().toString
}
