package Planning

import Debugging.SimpleString
import Lifecycle.With

class Plan extends Prioritized with SimpleString {
  
  protected def onUpdate() {}
  
  final def update() {
    With.prioritizer.prioritize(this)
    onUpdate()
  }
}