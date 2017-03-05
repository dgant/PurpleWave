package Plans.Compound

import Plans.Plan
import Utilities.Property

class Not extends Plan {
  
  val child = new Property[Plan](new Plan)
  
  override def isComplete: Boolean = ! child.get.isComplete
  override def onFrame() = child.get.onFrame()
}
