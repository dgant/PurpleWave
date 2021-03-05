package Planning.Predicates.Milestones

import Lifecycle.With
import Planning.Predicate

class BasesAtLeast(requiredBases: Int) extends Predicate {
  
  override def apply: Boolean = With.geography.ourBases.size >= requiredBases
  
}
