package Planning.Plans.Compound

import Planning.Composition.Property
import Planning.Plan

class Not(initialChild:Plan = new Plan) extends Plan {
  
  description.set("Not")
  
  val child = new Property[Plan](initialChild)
  
  override def isComplete: Boolean = ! child.get.isComplete
  override def update() = child.get.update()
}
