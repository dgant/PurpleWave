package Performance

import Lifecycle.With

import java.lang.ref.WeakReference

// For derived values that must not keep retired prediction cycles alive while the owner is idle.
class WeakCache[T >: Null <: AnyRef](getValue: () => T, refreshPeriod: Int = 1) {
  private var nextUpdateFrame = 0
  private var lastValue = new WeakReference[T](null)

  @inline final def apply(): T = synchronized {
    var value = lastValue.get
    if (With.frame >= nextUpdateFrame || value == null) {
      value = getValue()
      lastValue = new WeakReference(value)
      nextUpdateFrame = With.frame + refreshPeriod
    }
    value
  }

  @inline final def invalidate(): Unit = synchronized {
    nextUpdateFrame = With.frame
    lastValue.clear()
  }
}
