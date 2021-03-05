package Planning.Predicates.Strategy

import Lifecycle.With
import Planning.Predicate

class WeAreProtoss extends Predicate {
  
  override def apply: Boolean = With.self.isProtoss
  
}
