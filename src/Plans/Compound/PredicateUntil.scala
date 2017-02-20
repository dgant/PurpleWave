package Plans.Compound

import Plans.Plan
import Types.Property

class PredicateUntil extends Plan {
  
  description.set(Some("Do while a predicate is incomplete"))
  
  val predicate = new Property[Plan](new Plan)
  val child = new Property[Plan](new Plan)
  
  override def getChildren: Iterable[Plan] = { List(predicate.get) }
  override def isComplete: Boolean = { ! predicate.get.isComplete }
  
  override def onFrame() {
    predicate.get.onFrame()
    
    if ( ! predicate.get.isComplete) {
      child.get.onFrame()
    }
  }
}
