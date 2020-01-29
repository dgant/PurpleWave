package Planning.Plans.Army

import Lifecycle.With
import Planning.{Plan, Predicate, Property}
import Planning.Predicates.Always

class AllIn(initialPredicate: Predicate = new Always) extends Plan {
  
  val predicate: Property[Predicate] = new Property(initialPredicate)
  
  override def onUpdate() {
    With.blackboard.wantToAttack.set(true)
    With.blackboard.allIn.set(predicate.get.isComplete)
  }
}
