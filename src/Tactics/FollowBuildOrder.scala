package Tactics

import Lifecycle.With

class FollowBuildOrder extends Tactic {
  def launch() {
    With.buildPlans.update(this)
    With.buildPlans.getChildren.foreach(_.update())
  }
}
