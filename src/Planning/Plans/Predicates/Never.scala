package Planning.Plans.Predicates

import Planning.Predicate

class Never extends Predicate {
  override def isComplete: Boolean = false
}
