package Macro.Allocation

import Lifecycle.With

trait Prioritized {

  private var _priority: Int = Int.MaxValue
  private var _lastPrioritizationFrame: Int = Int.MinValue

  final def isPrioritized: Boolean = {
    _lastPrioritizationFrame >= With.priorities.lastResetFrame
  }

  final def priorityUntouched: Int = {
    if (isPrioritized) _priority else Int.MaxValue
  }

  final def prioritize(): Int = {
    if (isPrioritized) _priority else {
      _lastPrioritizationFrame = With.frame
      _priority = With.priorities.nextPriority()
      _priority
    }
  }
}
