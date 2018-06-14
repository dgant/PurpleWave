package Planning.Plans.Predicates.Milestones

import Lifecycle.With
import Planning.Predicate

class BasesAtLeast(requiredBases: Int) extends Predicate {
  
  override def isComplete: Boolean = With.geography.ourBases.size >= requiredBases
  
}
