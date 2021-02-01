package Planning.Plans.Macro.BuildOrders

import Lifecycle.With
import Planning.Prioritized

class FollowBuildOrder extends Prioritized {
  def update() {
    With.buildPlans.update(this)
    With.buildPlans.getChildren.foreach(_.update())
  }
}
