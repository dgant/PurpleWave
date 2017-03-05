package Plans.Compound

import Plans.Plan
import Utilities.Property

class While extends Plan {
  
  description.set(Some("Do while a predicate is complete"))
  
  val predicate = new Property[Plan](new Plan)
  val action = new Property[Plan](new Plan)
  
  override def getChildren: Iterable[Plan] = { List(predicate.get, action.get) }
  override def isComplete: Boolean = { getChildren.forall(_.isComplete) }
  
  override def onFrame() {
    predicate.get.onFrame()
    
    if (predicate.get.isComplete) {
      action.get.onFrame()
    }
  }
}
