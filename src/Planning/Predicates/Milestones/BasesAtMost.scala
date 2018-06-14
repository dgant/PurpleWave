package Planning.Predicates.Milestones

import Lifecycle.With
import Planning.Predicate

class BasesAtMost(requiredBases: Int) extends Predicate {
  
  override def isComplete: Boolean = With.geography.ourBases.size <= requiredBases
  
}
