package Planning

import Debugging.SimpleString
import Lifecycle.With

class Plan extends Prioritized with SimpleString {
  
  val description = new Property[String](super.toString)
  
  protected def onUpdate() {}
  
  final def update() {
    // Ideally we should only prioritize on demand
    // We're pretty close to supporting that
    With.prioritizer.prioritize(this)
    onUpdate()
  }
  
  override def toString: String = description.get
}