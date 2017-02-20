package Plans.Compound

import Plans.Plan
import Types.Property

class PredicateWhile extends Plan {
  
  description.set(Some("Do while a predicate is complete"))
  
  val predicate = new Property[Plan](new Plan)
  val child = new Property[Plan](new Plan)
  
  override def getChildren: Iterable[Plan] = { List(predicate.get) }
  override def isComplete: Boolean = { getChildren.forall(_.isComplete) }
  
  override def onFrame() {
    predicate.get.onFrame()
    
    if (predicate.get.isComplete) {
      child.get.onFrame()
    }
  }
}
