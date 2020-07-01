package Planning.Plans.Macro.BuildOrders

import Lifecycle.With
import Planning.Plan

class FollowBuildOrder extends Plan {
  override def onUpdate() {
    With.buildPlans.update(this)
  }
}
