package Planning.Predicates

import Planning.Predicate

class Never extends Predicate {
  override def apply: Boolean = false
}
