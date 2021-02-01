package Planning

import Lifecycle.With

trait Prioritized {
  final def isPrioritized: Boolean = With.prioritizer.isPrioritized(this)
  final def priority: Int = With.prioritizer.getPriority(this)
  final def prioritize(): Int = {
    With.prioritizer.prioritize(this)
    priority
  }
}
