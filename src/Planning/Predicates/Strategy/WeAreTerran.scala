package Planning.Predicates.Strategy

import Lifecycle.With
import Planning.Predicate

class WeAreTerran extends Predicate {
  
  override def isComplete: Boolean = With.self.isTerran
  
}
