package Planning.Plans.Compound

import Planning.Composition.Property
import Planning.Plan

class FlipIf(
  initialPredicate  : Plan = new Plan,
  inititialFirst    : Plan = new Plan,
  initialSecond     : Plan = new Plan)
    extends Plan {
  
  description.set("Flip if")
  
  val predicate = new Property[Plan](initialPredicate)
  val first  = new Property[Plan](inititialFirst)
  val second = new Property[Plan](initialSecond)
  
  override def getChildren: Iterable[Plan] = Vector(predicate.get, first.get, second.get)
  override def isComplete: Boolean = predicate.get.isComplete && first.get.isComplete
  
  override def onUpdate() {
    delegate(predicate.get)
    if (predicate.get.isComplete) {
      delegate(second.get)
      delegate(first.get)
    }
    else {
      delegate(first.get)
      delegate(second.get)
    }
  }
}
