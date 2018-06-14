package Planning.Predicates.Strategy

import Lifecycle.With
import Planning.Predicate

class WeAreZerg extends Predicate {
  
  override def isComplete: Boolean = With.self.isZerg
  
}
