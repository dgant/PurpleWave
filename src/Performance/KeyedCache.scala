package Performance

import Lifecycle.With

class KeyedCache[TValue, TKey](getValue: () => TValue, getKey: () => TKey, refreshPeriod: Int = 1) {
  private var nextUpdateFrame: Int = 0
  private var lastKey: TKey = _
  private var defaultKey: TKey = lastKey
  private var lastValue: TValue = _
  private val defaultValue: TValue = lastValue

  @inline final def apply(): TValue = {
    if (With.frame >= nextUpdateFrame || getKey() != lastKey) {
      nextUpdateFrame = With.frame + refreshPeriod
      lastValue = getValue()
      lastKey = getKey()
    }
    lastValue
  }

  @inline final def invalidate() {
    nextUpdateFrame = With.frame
    lastKey = defaultKey
    lastValue = defaultValue
  }

  override def toString: String = apply().toString
}
