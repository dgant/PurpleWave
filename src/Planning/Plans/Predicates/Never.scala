package Planning.Plans.Predicates

import Planning.Plan

class Never extends Plan {
  override def isComplete: Boolean = false
}
