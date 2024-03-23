package Planning

import Debugging.SimpleString
import Macro.Allocation.Prioritized

class Plan extends Prioritized with SimpleString {
  
  protected def onUpdate() {}
  
  final def update(): Unit = {
    prioritize()
    onUpdate()
  }

  final def apply(): Unit = {
    update()
  }
}