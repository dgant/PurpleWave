package Planning.Predicates.Strategy

import Lifecycle.With
import Planning.Predicate

class StartPositionsAtLeast(count: Int) extends Predicate {
  override def isComplete: Boolean = With.geography.startLocations.size >= count
}
