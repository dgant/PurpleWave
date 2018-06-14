package Planning.Plans.Army

import Lifecycle.With
import Planning.Composition.Property
import Planning.{Plan, Predicate}
import Planning.Plans.Predicates.Always

class AllIn(initialPredicate: Predicate = new Always) extends Plan {
  
  val predicate: Property[Predicate] = new Property(initialPredicate)
  
  override def onUpdate() {
    With.blackboard.allIn = predicate.get.isComplete
  }
}
