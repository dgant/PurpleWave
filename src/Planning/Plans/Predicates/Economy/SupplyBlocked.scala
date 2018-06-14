package Planning.Plans.Predicates.Economy

import Lifecycle.With
import Planning.Predicate

class SupplyBlocked extends Predicate {
  
  override def isComplete: Boolean = With.self.supplyUsed >= With.self.supplyTotal
  
}
