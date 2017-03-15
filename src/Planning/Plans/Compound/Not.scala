package Planning.Plans.Compound

import Planning.Composition.Property
import Planning.Plan

class Not extends Plan {
  
  description.set("Not")
  
  val child = new Property[Plan](new Plan)
  
  override def isComplete: Boolean = ! child.get.isComplete
  override def onFrame() = child.get.onFrame()
}
