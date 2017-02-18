package Plans.Compound

import Plans.Plan
import Types.Property

abstract class AbstractPlanFulfillRequirements() extends Plan {
  
  val checker   = new Property[Plan](new Plan)
  val fulfiller = new Property[Plan](new Plan)
  
  override def getChildren: Iterable[Plan] = { List(checker.get, fulfiller.get) }
  override def isComplete: Boolean = { checker.get.isComplete }
  
  override def onFrame() {
    checker.get.onFrame()
    if ( ! isComplete) {
      fulfiller.get.onFrame()
    }
  }
}
