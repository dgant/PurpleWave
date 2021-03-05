package Planning.Predicates.Strategy

import Lifecycle.With
import Planning.Predicate

class WeAreTerran extends Predicate {
  
  override def apply: Boolean = With.self.isTerran
  
}
