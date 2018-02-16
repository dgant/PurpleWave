package Planning.Plans.Army

import Lifecycle.With
import Planning.Composition.Property
import Planning.Plan
import Planning.Plans.Predicates.Always

class AllIn(initialPredicate: Plan = new Always) extends Plan {
  
  val predicate: Property[Plan] = new Property(initialPredicate)
  
  override def onUpdate() {
    With.blackboard.allIn = predicate.get.isComplete
  }
}
