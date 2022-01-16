package Planning.Plans.Army

import Lifecycle.With
import Planning.Predicates.Always
import Planning.{Plan, Predicate}

class AllInIf(predicate: Predicate = new Always) extends Plan {
  
  override def onUpdate() {
    With.blackboard.allIn.set(With.blackboard.allIn() || predicate.apply)
    With.yolo.updateBlackboard()
  }
}
