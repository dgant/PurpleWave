package Planning.Plans.Compound

import Planning.Composition.Property
import Planning.Plan

class Not(initialChild: Plan = NoPlan) extends Plan {
  
  description.set("Not")
  
  val child = new Property[Plan](initialChild)
  
  override def isComplete: Boolean = ! child.get.isComplete
  
  override def onUpdate() { delegate(child.get) }
}
