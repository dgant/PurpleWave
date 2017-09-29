package Planning.Plans.Macro.BuildOrders

import Lifecycle.With
import Planning.Plan

class FollowBuildOrder extends Plan {
  
  override def getChildren: Iterable[Plan] = With.buildOrder.getChildren
  
  override def onUpdate() {
    With.buildOrder.update(this)
    getChildren.foreach(delegate)
  }
}
