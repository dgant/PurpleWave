package Planning

import Debugging.SimpleString

class Plan extends Prioritized with SimpleString {
  
  protected def onUpdate() {}
  
  final def update() {
    prioritize()
    onUpdate()
  }
}