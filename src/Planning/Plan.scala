package Planning

import Debugging.ToString
import Lifecycle.With

class Plan extends Prioritized {
  
  val description = new Property[String](realName)
  
  protected def onUpdate() {}
  
  final def update() {
    // Ideally we should only prioritize on demand
    // We're pretty close to supporting that
    With.prioritizer.prioritize(this)
    onUpdate()
  }
  
  final lazy val realName: String = ToString(this)
  
  final override def toString: String =
    if (realName == "")
      description.get
    else
      if (description.get == "") realName else description.get
}