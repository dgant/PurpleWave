package Plans.Compound

import Plans.Plan
import Utilities.Property

class JustOnce extends Plan {
  
  description.set("Do once")
    
  val child = new Property[Plan](new Plan)
  
  var _everCompleted:Boolean = false
  
  override def isComplete: Boolean = { _everCompleted }
  override def getChildren: Iterable[Plan] = { List(child.get) }
  override def onFrame() {
    _everCompleted = _everCompleted || child.get.isComplete
    
    if ( ! isComplete) {
      child.get.onFrame()
    }
  }
}
