package Plans.Compound

import Plans.Plan
import Types.Property

class CompleteOnce extends Plan {
    
  val child = new Property[Plan](new Plan)
  
  var _everCompleted:Boolean = false
  
  override def isComplete: Boolean = { _everCompleted }
  override def getChildren: Iterable[Plan] = { List(child.get) }
  override def onFrame(): Unit = {
    _everCompleted = _everCompleted || child.get.isComplete
    
    if ( ! isComplete) {
      child.get.onFrame()
    }
  }
}
