package Planning.Plans.Compound

import Planning.Composition.Property
import Planning.Plan

class JustOnce(initialChild:Plan = new Plan) extends Plan {
  
  description.set("Do once")
    
  val child = new Property[Plan](initialChild)
  
  var _everCompleted:Boolean = false
  
  override def isComplete: Boolean = _everCompleted
  override def getChildren: Iterable[Plan] = Vector(child.get)
  override def onUpdate() {
    _everCompleted ||= child.get.isComplete
    
    if ( ! isComplete) {
      delegate(child.get)
    }
  }
}
