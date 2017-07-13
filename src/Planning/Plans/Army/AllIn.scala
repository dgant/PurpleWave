package Planning.Plans.Army

import Lifecycle.With
import Planning.Plan

class AllIn extends Plan {
  
  override def onUpdate() {
    With.blackboard.allIn = true
  }
}
