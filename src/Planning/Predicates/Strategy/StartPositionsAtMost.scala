package Planning.Predicates.Strategy

import Lifecycle.With
import Planning.Predicate

class StartPositionsAtMost(count: Int) extends Predicate {
  override def apply: Boolean = With.geography.startLocations.size <= count
}