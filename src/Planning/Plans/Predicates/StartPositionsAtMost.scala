package Planning.Plans.Predicates

import Lifecycle.With
import Planning.Predicate

class StartPositionsAtMost(count: Int) extends Predicate {
  override def isComplete: Boolean = With.geography.startLocations.size <= count
}