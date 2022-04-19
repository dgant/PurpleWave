package Planning.Plans.Army

import Lifecycle.With
import Planning.Predicates.{Always, Predicate}
import Planning.Plan

class AllInIf(predicate: Predicate = new Always) extends Plan {
  
  override def onUpdate() {
    if (predicate.apply) With.blackboard.yoloing.set(true)
    With.yolo.updateBlackboard()
  }
}
