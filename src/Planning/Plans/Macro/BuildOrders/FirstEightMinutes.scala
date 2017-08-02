package Planning.Plans.Macro.BuildOrders

import Lifecycle.With
import Planning.Composition.Property
import Planning.Plan

class FirstEightMinutes(initialChild: Plan) extends Plan {
  
  val child: Property[Plan] = new Property(initialChild)
  
  override def getChildren: Iterable[Plan] = List(child.get)
  override def isComplete: Boolean = With.frame > 24 * 60 * 8
  
  override def onUpdate() {
    if ( ! isComplete) {
      child.get.update()
    }
  }
}
