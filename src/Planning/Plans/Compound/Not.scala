package Planning.Plans.Compound

import Planning.Composition.Property
import Planning.{Plan, Predicate}

class Not(initialChild: Predicate) extends Predicate {
  
  description.set("Not")
  
  val child = new Property[Plan](initialChild)
  
  override def isComplete: Boolean = ! child.get.isComplete
  
  override def toString: String = super.toString + ": " + child.get
}
